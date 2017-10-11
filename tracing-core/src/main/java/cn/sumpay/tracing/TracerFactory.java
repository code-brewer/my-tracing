package cn.sumpay.tracing;

import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author heyc
 */
public interface TracerFactory {

    Logger LOG = LoggerFactory.getLogger(TracerFactory.class);

    TracerFactory DEFAULT = new DefaultTracerFactory();

    /**
     * get a Tracer implementation. this method may called every request, consider whether singleton
     * pattern is needed
     *
     * @return
     */
    Tracer getTracer();

    class DefaultTracerFactory implements TracerFactory {

        private static Tracer tracer =  NoopTracerFactory.create();

        static {
            loadDefaultTracer();
        }
        /**
         * load SPI Tracer and set default only if one tracer is found.
         */
        private static void loadDefaultTracer() {
            try {
                Iterator<TracerBuilder> implementations = ServiceLoader.load(TracerBuilder.class, TracerBuilder.class.getClassLoader()).iterator();
                if (implementations.hasNext()) {
                    tracer = implementations.next().build();
                }
            } catch (Exception e) {
                LOG.warn("DefaultTracerFactory load Tracer fail.", e);
            }
        }

        @Override
        public Tracer getTracer() {
            return tracer;
        }
    }
}
