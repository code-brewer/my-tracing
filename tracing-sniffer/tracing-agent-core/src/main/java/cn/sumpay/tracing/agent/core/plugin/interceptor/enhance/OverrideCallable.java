package cn.sumpay.tracing.agent.core.plugin.interceptor.enhance;

/**
 * @author heyc
 */
public interface OverrideCallable {
    Object call(Object[] args);
}
