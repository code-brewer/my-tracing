package cn.sumpay.tracing;

import io.opentracing.Tracer;

/**
 * @author heyc
 */
public interface TracerBuilder {

    Tracer build();

}
