package cn.sumpay.tracing;

import cn.sumpay.tracing.interceptor.TracingHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author heyc
 */
@ConditionalOnClass(TracingHandlerInterceptor.class)
@EnableConfigurationProperties(TracingProperties.class)
@Configuration
public class TracingAutoInerceptor extends WebMvcConfigurerAdapter {

    @Autowired
    TracingProperties tracingProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        TracingHandlerInterceptor interceptor = new TracingHandlerInterceptor();
        interceptor.setWebEnable(tracingProperties.isWebEnable());
        interceptor.setRequestEnable(tracingProperties.isWebRequestEnable());
        registry.addInterceptor(interceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
