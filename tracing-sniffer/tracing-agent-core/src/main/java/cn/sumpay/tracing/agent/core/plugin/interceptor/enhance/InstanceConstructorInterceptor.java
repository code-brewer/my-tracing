package cn.sumpay.tracing.agent.core.plugin.interceptor.enhance;

/**
 * The instance constructor's interceptor interface.
 * Any plugin, which wants to intercept constructor, must implement this interface.
 * <p>
 *
 * @author heyc
 */
public interface InstanceConstructorInterceptor {
    /**
     * Called before the origin constructor invocation.
     */
    void onConstruct(EnhancedInstance objInst, Object[] allArguments);
}
