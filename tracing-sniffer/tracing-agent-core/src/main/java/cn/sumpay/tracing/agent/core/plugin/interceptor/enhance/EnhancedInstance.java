package cn.sumpay.tracing.agent.core.plugin.interceptor.enhance;

/**
 * @author heyc
 */
public interface EnhancedInstance {

    Object getSkyWalkingDynamicField();

    void setSkyWalkingDynamicField(Object value);
}
