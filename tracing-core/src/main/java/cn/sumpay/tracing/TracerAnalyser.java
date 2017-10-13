package cn.sumpay.tracing;

import io.opentracing.Span;

/**
 * @author heyc
 */
public interface TracerAnalyser {

    String findTraceId(Span span);

}
