package cn.sumpay.tracing.context;

import io.opentracing.Span;

/**
 * @author heyc
 */
public interface TracingContext {

    Span getTracingSpan();

    void setTracingSpan(Span span);

}
