package cn.sumpay.tracing.dubbo;

import cn.sumpay.tracing.TracerAnalyser;
import cn.sumpay.tracing.context.TracingContext;
import cn.sumpay.tracing.util.JsonUtil;
import com.alibaba.dubbo.rpc.RpcContext;
import io.opentracing.Span;

/**
 * @author heyc
 */
public class RpcTracingContext implements TracingContext{

    public static final String TRACING_SPAN = "rpc_tracing_span";

    private RpcTracingContext(){
    }

    private static RpcTracingContext instance;

    /**
     * getInstance
     * @return
     */
    public static RpcTracingContext getInstance(){
        if (instance == null){
            synchronized (RpcTracingContext.class){
                if (instance == null){
                    instance = new RpcTracingContext();
                }
            }
        }
        return instance;
    }

    @Override
    public Span getTracingSpan() {
        String spanJson = RpcContext.getContext().getAttachment(TRACING_SPAN);
        if (spanJson != null && !(spanJson == null || spanJson.length() == 0)) {
            return JsonUtil.parseObject(spanJson,Span.class);
        }
        return null;
    }

    @Override
    public void setTracingSpan(Span span) {
        RpcContext.getContext().setAttachment(TRACING_SPAN, JsonUtil.toJsonString(span));
    }

    @Override
    public void removeTracingSpan() {
        RpcContext.getContext().removeAttachment(TRACING_SPAN);
    }

    @Override
    public void setTracerAnalysis(TracerAnalyser tracerAnalyser) {

    }
}
