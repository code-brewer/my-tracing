package cn.sumpay.tracing;

import cn.sumpay.tracing.trace.TraceAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author heyc
 */
@Configuration
@ConditionalOnClass(TraceAspect.class)
public class TracingAutoConfiguration {

    @Bean
    @Order(10000)
    public TraceAspect traceAspect(){
        return new TraceAspect();
    }

}
