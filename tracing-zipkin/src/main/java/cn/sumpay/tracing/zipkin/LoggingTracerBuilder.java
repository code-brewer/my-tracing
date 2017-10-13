package cn.sumpay.tracing.zipkin;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import cn.sumpay.tracing.TracerBuilder;
import cn.sumpay.tracing.TracerConfig;
import cn.sumpay.tracing.zipkin.reporter.LoggingReporter;
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
