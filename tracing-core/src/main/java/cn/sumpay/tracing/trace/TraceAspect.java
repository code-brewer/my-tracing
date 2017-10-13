package cn.sumpay.tracing.trace;

import cn.sumpay.tracing.TracerAttachment;
import cn.sumpay.tracing.TracerConfig;
import cn.sumpay.tracing.TracerFactory;
import cn.sumpay.tracing.context.TracingContext;
import com.alibaba.fastjson.JSONObject;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author heyc
 */
@Aspect
public class TraceAspect {

    private static final Logger LOG = LoggerFactory.getLogger(TraceAspect.class);
    /**
     * 切入点 @Trace 方法
     */
    @Pointcut("@annotation(cn.sumpay.tracing.trace.Trace)")
    public void traceAspect() {
    }

    /**
     * 环绕通知 用于拦截方法调用
     * @param joinPoint 切点
     */
    @Around("traceAspect() && @annotation(trace)")
    public Object doAround(ProceedingJoinPoint joinPoint,Trace trace) throws Throwable {
        if (!TracerConfig.ENABLE){
            return joinPoint.proceed();
        }
        Span origSpan = null;
        Span span = null;
        try {
            /** trace **/
            if (trace != null){
                /** clearTrace **/
                if (trace.entry()){
                    TracingContext.removeSpan();
                }
                Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
                String spanName = (trace.operationName()==null||"".equals(trace.operationName()))?method.getName():trace.operationName();
                Tracer.SpanBuilder spanBuilder = TracerFactory.DEFAULT.getTracer().buildSpan(spanName);
                origSpan = TracingContext.getSpan();
                if (origSpan != null) {
                    spanBuilder.withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_SERVER);
                    spanBuilder.asChildOf(origSpan);
                }else {
                    spanBuilder.withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_CLIENT);
                }
                span = spanBuilder.startManual();
                if(TracerConfig.REQUEST && trace.request()){
                    span.setTag("request", JSONObject.toJSONString(joinPoint.getArgs()));
                }
                span.setTag(TracerAttachment.TYPE,"local");
                span.setTag(TracerAttachment.METHOD,method.getName());
                span.setTag("class",method.getDeclaringClass().getName());
                TracingContext.setSpan(span);
            }
        }catch (Exception e){
            LOG.error("TraceAspect error:{}",e.getMessage());
        }
        Object result = joinPoint.proceed();
        try {
            if (trace != null){
                if (TracerConfig.RESPONSE && trace.response()){
                    span.setTag("response",JSONObject.toJSONString(result));
                }
                span.finish();
                if (origSpan != null){
                    TracingContext.setSpan(origSpan);
                }else {
                    TracingContext.removeSpan();
                }
            }
        }catch (Exception e){
            LOG.error("TraceAspect error:{}",e.getMessage());
        }
        return result;
    }
}
