package cn.sumpay.tracing.zipkin.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

/**
 * @author heyc
 */
public class LoggingReporter implements Reporter<Span> {

    private static Logger tracerLogger = LoggerFactory.getLogger("trace");

    @Override
    public void report(Span span) {
        if (span != null){
            tracerLogger.info(span.toString());
        }
    }
}
