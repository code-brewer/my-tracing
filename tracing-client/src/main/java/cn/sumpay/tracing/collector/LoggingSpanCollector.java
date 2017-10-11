package cn.sumpay.tracing.collector;

import com.github.kristofa.brave.SpanCollector;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.kristofa.brave.internal.Util.checkNotBlank;
import static com.github.kristofa.brave.internal.Util.checkNotNull;

/**
 * @author heyc
 */
public class LoggingSpanCollector implements SpanCollector {

    private final Logger logger;

    private final Set<BinaryAnnotation> defaultAnnotations = new LinkedHashSet<BinaryAnnotation>();

    public LoggingSpanCollector() {
        logger = LoggerFactory.getLogger(LoggingSpanCollector.class);
    }

    public LoggingSpanCollector(String loggerName) {
        checkNotBlank(loggerName, "Null or blank loggerName");
        logger = LoggerFactory.getLogger(loggerName);
    }

    /**
     * collect
     * @param span
     */
    @Override
    public void collect(final Span span) {
        checkNotNull(span, "Null span");
        if (!defaultAnnotations.isEmpty()) {
            for (final BinaryAnnotation ba : defaultAnnotations) {
                span.addToBinary_annotations(ba);
            }
        }
        logger.info(span.toString());
    }

    /**
     * addDefaultAnnotation
     * @param key
     * @param value
     */
    @Override
    public void addDefaultAnnotation(final String key, final String value) {
        defaultAnnotations.add(BinaryAnnotation.create(key, value, null));
    }
}
