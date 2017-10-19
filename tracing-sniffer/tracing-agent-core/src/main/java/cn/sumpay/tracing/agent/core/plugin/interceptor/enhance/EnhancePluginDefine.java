package cn.sumpay.tracing.agent.core.plugin.interceptor.enhance;

import cn.sumpay.tracing.agent.core.plugin.PluginException;
import cn.sumpay.tracing.agent.core.plugin.match.ClassMatch;
import net.bytebuddy.dynamic.DynamicType;

/**
 * @author heyc
 * @date 2017/10/19 16:26
 */
public interface EnhancePluginDefine {

    DynamicType.Builder<?> define(String transformClassName, DynamicType.Builder<?> builder, ClassLoader classLoader) throws PluginException;

    DynamicType.Builder<?> enhance(String enhanceOriginClassName, DynamicType.Builder<?> newClassBuilder, ClassLoader classLoader) throws PluginException;

    ClassMatch enhanceClass();

}
