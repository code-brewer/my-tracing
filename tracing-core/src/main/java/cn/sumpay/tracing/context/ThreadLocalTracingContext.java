package cn.sumpay.tracing.context;

import cn.sumpay.tracing.TracerAnalyser;
import cn.sumpay.tracing.TracerConfig;
import io.opentracing.Span;
import org.slf4j.MDC;

/**
 * @author heyc
 */
public class ThreadLocalTracingContext implements TracingContext{

    private static ThreadLocal<Span> LOCAL_SPAN = new ThreadLocal<Span>();

    private ThreadLocalTracingContext(){
    }

    private static ThreadLocalTracingContext instance;

    private TracerAnalyser tracerAnalyser;

    /**
     * getInstance
     * @return
     */
    public static ThreadLocalTracingContext getInstance(){
        if (instance == null){
            synchronized (ThreadLocalTracingContext.class){
                if (instance == null){
                    instance = new ThreadLocalTracingContext();
                }
            }
        }
        return instance;
    }

    @Override
    public Span getTracingSpan() {
        return LOCAL_SPAN.get();
    }

    @Override
    public void setTracingSpan(Span span) {
        LOCAL_SPAN.set(span);
        if(TracerConfig.MDCENABLE && tracerAnalyser != null){
            MDC.put(TRACE_MDC,tracerAnalyser.findTraceId(span));
        }
    }

    @Override
    public void removeTracingSpan() {
        LOCAL_SPAN.remove();
        if(TracerConfig.MDCENABLE){
            MDC.remove(TRACE_MDC);
        }
    }

    @Override
    public void setTracerAnalysis(TracerAnalyser tracerAnalyser) {
        this.tracerAnalyser = tracerAnalyser;
    }
}
