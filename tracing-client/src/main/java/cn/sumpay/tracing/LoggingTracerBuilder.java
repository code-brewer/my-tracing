package cn.sumpay.tracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import cn.sumpay.tracing.reporter.LoggingReporter;
import io.opentracing.Tracer;

/**
 * @author heyc
 */
public class LoggingTracerBuilder implements TracerBuilder{

    @Override
    public Tracer build() {
        return BraveTracer.create(Tracing.newBuilder().localServiceName(TracerConfig.APPLICATION==null?"unknown":TracerConfig.APPLICATION).spanReporter(new LoggingReporter()).build());
    }
}
