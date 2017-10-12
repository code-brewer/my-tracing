package cn.sumpay.tracing.dubbo.filter;

import cn.sumpay.tracing.TracerAttachment;
import cn.sumpay.tracing.TracerFactory;
import cn.sumpay.tracing.TracerConfig;
import cn.sumpay.tracing.context.TracingContext;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import io.opentracing.NoopTracer;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author heyc
 */
@Activate(group = Constants.PROVIDER)
public class TracerProviderFilter implements Filter{

    private static final Logger LOG = LoggerFactory.getLogger(TracerProviderFilter.class);

    /**
     * 拦截dubbo调用
     * @param invoker
     * @param invocation
     * @return
     * @throws RpcException
     */
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        /** 不记录 **/
        Tracer tracer = TracerFactory.DEFAULT.getTracer();
        if (!TracerConfig.ENABLE || tracer == null || tracer instanceof NoopTracer){
            return invoker.invoke(invocation);
        }
        Span span = null;
        try {
            span = extractTraceInfo(invocation, tracer);
            TracingContext.setSpan(span);
        }catch (Exception e){
            LOG.error("获取span异常: {}",e.getMessage());
        }
        /** 方法调用 **/
        Result result = null;
        try {
            result = invoker.invoke(invocation);
        }finally {
            try {
                if (span != null){
                    span.setTag(TracerAttachment.TYPE,"dubbo");
                    span.setTag(TracerAttachment.METHOD,invocation.getMethodName());
                    span.setTag("remoteIp",invoker.getUrl().getIp());
                    span.setTag("interface",invocation.getInvoker().getInterface().getName());
                    span.finish();
                }
            }catch (Exception e){
                LOG.error("END span ERROR: {}",e.getMessage());
            }
        }
        return result;
    }


    /**
     * extractTraceInfo
     * @param invocation
     * @param tracer
     * @return
     */
    protected Span extractTraceInfo(Invocation invocation, Tracer tracer) {
        String application = RpcContext.getContext().getUrl().getParameter("application");
        String operationName = application + "_" + invocation.getMethodName();
        Tracer.SpanBuilder span = tracer.buildSpan(operationName).withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_SERVER);
        try {
            SpanContext spanContext = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(invocation.getAttachments()));
            if (spanContext != null) {
                span.asChildOf(spanContext);
            }
        } catch (Exception e) {
            span.withTag("Error", "extract from request fail, error msg:" + e.getMessage());
        }
        return span.startManual();
    }
}
