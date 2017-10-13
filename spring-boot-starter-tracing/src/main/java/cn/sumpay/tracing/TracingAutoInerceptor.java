package cn.sumpay.tracing;

import cn.sumpay.tracing.interceptor.TracingHandlerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author heyc
 */
@ConditionalOnClass(TracingHandlerInterceptor.class)
@Configuration
public class TracingAutoInerceptor extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TracingHandlerInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
