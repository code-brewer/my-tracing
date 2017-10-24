package cn.sumpay.tracing.agent;

import cn.sumpay.tracing.agent.core.boot.ServiceManager;
import cn.sumpay.tracing.agent.core.conf.SnifferConfigInitializer;
import cn.sumpay.tracing.agent.core.logger.BootLogger;
import cn.sumpay.tracing.agent.core.plugin.PluginBootstrap;
import cn.sumpay.tracing.agent.core.plugin.PluginFinder;
import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.EnhancePluginDefine;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author heyc
 * @date 2017/10/19 11:45
 */
public class TracingAgent {

    private static final BootLogger logger = BootLogger.getLogger(TracingAgent.class);

    private TracingAgent() {
        throw new InstantiationError( "Must not instantiate this class" );
    }

    private static Set<String> bootstrapJars = new HashSet<String>();

    /**
     * premain
     * @param agentArgs
     * @param instrumentation
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {

        logger.info("******************** [agent start] ********************");

        logger.info("agentArgs: "+agentArgs+"  instrumentation: " + instrumentation);

        logger.info("******************** [SnifferConfigInitializer.initialize] start.");
        SnifferConfigInitializer.initialize();
        logger.info("******************** [SnifferConfigInitializer.initialize] success.");

        final PluginFinder pluginFinder = new PluginFinder(new PluginBootstrap().spiLoad());

        logger.info("******************** [service boot] start.");
        ServiceManager.INSTANCE.boot();
        logger.info("******************** [service boot] success.");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override public void run() {
                ServiceManager.INSTANCE.shutdown();
            }
        }, "tracing service shutdown thread"));

        logger.info("******************** [plugin transform] start.");
        new AgentBuilder.Default().type(pluginFinder.buildMatch()).transform(new AgentBuilder.Transformer() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                EnhancePluginDefine pluginDefine = pluginFinder.find(typeDescription, classLoader);
                if (pluginDefine != null) {
                    DynamicType.Builder<?> newBuilder = pluginDefine.define(typeDescription.getTypeName(), builder, classLoader);
                    if (newBuilder != null) {
                        logger.info("Finish the prepare stage for " + typeDescription.getName());
                        return newBuilder;
                    }
                }
                logger.info("Matched class " + typeDescription.getTypeName() + ", but ignore by finding mechanism.");
                return builder;
            }
        }).with(new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

            }
            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
                    logger.info("On Transformation class " + typeDescription.getName());
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
            }

            @Override public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                logger.warn("Failed to enhance class " + typeName, throwable);
            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                //logger.info("onComplete class " + typeName);
            }
        }).installOn(instrumentation);
        logger.info("******************** [plugin transform] success.");

        logger.info("******************** [agent end] ********************");
    }
}
