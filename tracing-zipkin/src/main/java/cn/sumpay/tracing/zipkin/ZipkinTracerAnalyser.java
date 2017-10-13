package cn.sumpay.tracing.zipkin;

import brave.opentracing.BraveSpanContext;
import cn.sumpay.tracing.TracerAnalyser;
import io.opentracing.Span;

/**
 * @author heyc
 */
public class ZipkinTracerAnalyser implements TracerAnalyser{

    @Override
    public String findTraceId(Span span) {
        return Long.toHexString(((BraveSpanContext)span.context()).unwrap().traceId());
    }
}
