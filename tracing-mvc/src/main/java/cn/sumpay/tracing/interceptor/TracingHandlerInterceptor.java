package cn.sumpay.tracing.interceptor;

import cn.sumpay.tracing.TracerAttachment;
import cn.sumpay.tracing.TracerConfig;
import cn.sumpay.tracing.TracerFactory;
import cn.sumpay.tracing.context.TracingContext;
import com.alibaba.fastjson.JSONObject;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author heyc
 */
public class TracingHandlerInterceptor extends HandlerInterceptorAdapter {

    private Logger LOG = LoggerFactory.getLogger(TracingHandlerInterceptor.class);

    private static final String SPAN_MVC = "span-mvc";

    public TracingHandlerInterceptor(){
        this(TracerFactory.DEFAULT.getTracer());
    }

    public TracingHandlerInterceptor(Tracer tracer){
        super();
        this.tracer = tracer;
    }

    private Tracer tracer;

    private ThreadLocal<Span> spanThreadLocal = new ThreadLocal<Span>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!TracerConfig.ENABLE || request.getAttribute(SPAN_MVC) != null) {
            return true; // already handled (possibly due to async request)
        }
        try {
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(request.getServletPath()).withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_SERVER);
            Span span = spanBuilder.startManual();
            if (TracerConfig.REQUEST){
                span.setTag("request",JSONObject.toJSONString(request));
            }
            span.setTag(TracerAttachment.TYPE,"mvc");
            span.setTag(TracerAttachment.METHOD,request.getRequestURI());
            span.setTag("remoteAddr",request.getRemoteAddr());
            spanThreadLocal.set(span);
        }catch (Exception e){
            LOG.error("preHandle span error: " + e.getMessage());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        try {
            TracingContext.removeSpan();
            Span span = spanThreadLocal.get();
            if (span != null){
                if (TracerConfig.RESPONSE){
                    span.setTag("response", JSONObject.toJSONString(response));
                }
                span.finish();
                spanThreadLocal.remove();
            }
        }catch (Exception e){
            LOG.error("stop span error: " + e.getMessage());
        }
    }
}
