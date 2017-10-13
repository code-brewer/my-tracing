package cn.sumpay.tracing.context;

import cn.sumpay.tracing.TracerAnalyser;
import io.opentracing.Span;

/**
 * @author heyc
 */
public interface TracingContext {

    String TRACE_MDC = "traceId";

    static Span getSpan(){
        return ThreadLocalTracingContext.getInstance().getTracingSpan();
    }

    static void setSpan(Span span){
        ThreadLocalTracingContext.getInstance().setTracingSpan(span);
    }

    static void removeSpan(){
        ThreadLocalTracingContext.getInstance().removeTracingSpan();
    }

    static void setTracerAnalyser(TracerAnalyser tracerAnalyser) {
        ThreadLocalTracingContext.getInstance().setTracerAnalysis(tracerAnalyser);
    }

    Span getTracingSpan();

    void setTracingSpan(Span span);

    void removeTracingSpan();

    void setTracerAnalysis(TracerAnalyser tracerAnalyser);

}
