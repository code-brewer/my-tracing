package cn.sumpay.tracing.dubbo;

import cn.sumpay.tracing.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import cn.sumpay.tracing.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import cn.sumpay.tracing.agent.core.plugin.match.ClassMatch;
import cn.sumpay.tracing.agent.core.plugin.match.NameMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * {@link DubboInstrumentation} presents that skywalking intercepts {@link com.alibaba.dubbo.monitor.support.MonitorFilter#invoke(com.alibaba.dubbo.rpc.Invoker,
 * com.alibaba.dubbo.rpc.Invocation)} by using {@link DubboInterceptor}.
 *
 * @author heyc
 */
public class DubboInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "com.alibaba.dubbo.monitor.support.MonitorFilter";
    private static final String INTERCEPT_CLASS = "cn.sumpay.tracing.dubbo.DubboInterceptor";

    @Override
    public ClassMatch enhanceClass() {
        return NameMatch.byName(ENHANCE_CLASS);
    }

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return ElementMatchers.named("invoke");
                }

                @Override
                public String getMethodsInterceptor() {
                    return INTERCEPT_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }
}
