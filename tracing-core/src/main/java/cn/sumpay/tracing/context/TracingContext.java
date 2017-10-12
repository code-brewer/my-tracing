package cn.sumpay.tracing.context;

import io.opentracing.Span;

/**
 * @author heyc
 */
public interface TracingContext {

    static Span getSpan(){
        return ThreadLocalTracingContext.getInstance().getTracingSpan();
    }

    static void setSpan(Span span){
        ThreadLocalTracingContext.getInstance().setTracingSpan(span);
    }

    Span getTracingSpan();

    void setTracingSpan(Span span);

}
