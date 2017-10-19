package cn.sumpay.tracing.dubbo;

import cn.sumpay.tracing.TracerAttachment;
import cn.sumpay.tracing.TracerConfig;
import cn.sumpay.tracing.TracerFactory;
import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cn.sumpay.tracing.context.TracingContext;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSONObject;
import io.opentracing.NoopTracer;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

/**
 * {@link DubboInterceptor} define how to enhance class {@link com.alibaba.dubbo.monitor.support.MonitorFilter#invoke(Invoker,
 * Invocation)}. the trace context transport to the provider side by {@link RpcContext#attachments}.but all the version
 * of dubbo framework below 2.8.3 don't support {@link RpcContext#attachments}, we support another way to support it.
 *
 * @author heyc
 */
public class DubboInterceptor implements InstanceMethodsAroundInterceptor {

    private Logger logger = LoggerFactory.getLogger(DubboInterceptor.class);

    private ThreadLocal<Span> localSpan = new ThreadLocal<Span>();
    /**
     * <h2>Consumer:</h2> The serialized trace context data will
     * inject to the {@link RpcContext#attachments} for transport to provider side.
     * <p>
     * <h2>Provider:</h2> The serialized trace context data will extract from
     * {@link RpcContext#attachments}. current trace segment will ref if the serialize context data is not null.
     */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments,Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        try {
            Tracer tracer = TracerFactory.DEFAULT.getTracer();
            if (!TracerConfig.ENABLE || tracer == null || tracer instanceof NoopTracer){
                return;
            }
            Invoker invoker = (Invoker)allArguments[0];
            Invocation invocation = (Invocation)allArguments[1];
            RpcContext rpcContext = RpcContext.getContext();
            boolean isConsumer = rpcContext.isConsumerSide();
            Span span;
            String operationName = rpcContext.getUrl().getParameter("application") + "_" + method.getName();
            if (isConsumer){
                Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationName).withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_CLIENT);
                Span activeSpan = TracingContext.getSpan();
                if (activeSpan != null) {
                    spanBuilder.asChildOf(activeSpan);
                }
                span = spanBuilder.startManual();
                /** 属性传递 **/
                attachTraceInfo(tracer, span, invocation);
            }else {
                Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationName).withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_SERVER);
                SpanContext spanContext = extractSpanContext(invocation, tracer);
                if (spanContext != null){
                    spanBuilder.asChildOf(spanContext);
                }
                span = spanBuilder.startManual();
                TracingContext.setSpan(span);
            }
            localSpan.set(span);
            /** 添加属性 **/
            if(TracerConfig.REQUEST){
                span.setTag("request", JSONObject.toJSONString(invocation.getArguments()));
            }
            span.setTag(TracerAttachment.TYPE,"dubbo");
            span.setTag(TracerAttachment.METHOD,invocation.getMethodName());
            span.setTag("remoteIp",invoker.getUrl().getIp());
            span.setTag("interface",invocation.getInvoker().getInterface().getName());
        }catch (Exception e){
            logger.error("tracing beforeMethod 异常: {}",e.getMessage());
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        try {
            Span span = localSpan.get();
            if (span != null){
                if (TracerConfig.RESPONSE){
                    span.setTag("response",JSONObject.toJSONString(ret));
                }
                span.finish();
            }
        }catch (Exception e){
            logger.error("tracing afterMethod 异常: {}",e.getMessage());
        }finally {
            localSpan.remove();
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        try {
            Span span = localSpan.get();
            if (span != null){
                span.setTag("error",t.getMessage());
                span.finish();
            }
        }catch (Exception e){
            logger.error("tracing afterMethod 异常: {}",e.getMessage());
        }finally {
            localSpan.remove();
        }
    }


    /**
     * attachTraceInfo
     * @param tracer
     * @param span
     * @param invocation
     */
    private void attachTraceInfo(Tracer tracer, Span span, final Invocation invocation) {
        tracer.inject(span.context(), Format.Builtin.TEXT_MAP, new TextMap() {
            @Override
            public void put(String key, String value) {
                invocation.getAttachments().put(key, value);
            }
            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                throw new UnsupportedOperationException("TextMapInjectAdapter should only be used with Tracer.inject()");
            }
        });
    }

    /**
     * extractSpanContext
     * @param invocation
     * @param tracer
     * @return
     */
    protected SpanContext extractSpanContext(Invocation invocation, Tracer tracer) {
        try {
            return tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(invocation.getAttachments()));
        } catch (Exception e) {
            logger.error("extractSpanContext error: {}",e.getMessage());
        }
        return null;
    }
}
