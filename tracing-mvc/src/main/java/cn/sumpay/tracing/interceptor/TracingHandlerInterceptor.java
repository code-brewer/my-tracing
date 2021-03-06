package cn.sumpay.tracing.interceptor;

import cn.sumpay.tracing.TracerAttachment;
import cn.sumpay.tracing.TracerConfig;
import cn.sumpay.tracing.TracerFactory;
import cn.sumpay.tracing.context.TracingContext;
import cn.sumpay.tracing.util.JsonUtil;
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

    private boolean requestEnable = false;

    private boolean responseEnable = false;

    private boolean webEnable = true;

    public void setRequestEnable(boolean requestEnable) {
        this.requestEnable = requestEnable;
    }

    public void setResponseEnable(boolean responseEnable) {
        this.responseEnable = responseEnable;
    }

    public void setWebEnable(boolean webEnable) {
        this.webEnable = webEnable;
    }

    public TracingHandlerInterceptor(){
        this(TracerFactory.DEFAULT.getTracer());
    }

    public TracingHandlerInterceptor(Tracer tracer){
        super();
        this.tracer = tracer;
    }

    private Tracer tracer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!TracerConfig.ENABLE || !webEnable) {
            return true; // already handled (possibly due to async request)
        }
        try {
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(request.getServletPath()).withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_CLIENT);
            Span span = spanBuilder.startManual();
            if (TracerConfig.REQUEST && requestEnable){
                span.setTag("request", JsonUtil.toJsonString(request.getParameterMap()));
            }
            span.setTag(TracerAttachment.TYPE,"mvc");
            span.setTag(TracerAttachment.METHOD,request.getRequestURI());
            span.setTag("remoteAddr",request.getRemoteAddr());
            TracingContext.setSpan(span);
            span.finish();
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
        }catch (Exception e){
            LOG.error("stop span error: " + e.getMessage());
        }
    }
}
