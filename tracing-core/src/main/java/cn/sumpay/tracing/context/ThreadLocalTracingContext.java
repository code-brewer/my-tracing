package cn.sumpay.tracing.context;

import io.opentracing.Span;

/**
 * @author heyc
 */
public class ThreadLocalTracingContext implements TracingContext{

    private static ThreadLocal<Span> LOCAL_SPAN = new ThreadLocal<Span>();

    private ThreadLocalTracingContext(){
    }

    private static ThreadLocalTracingContext instance;

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
    }

    @Override
    public void removeTracingSpan() {
        LOCAL_SPAN.remove();
    }
}
