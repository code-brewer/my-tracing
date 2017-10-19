package cn.sumpay.tracing.agent.core.plugin;

import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.EnhancePluginDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author heyc
 */
public class PluginBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(PluginBootstrap.class);

    /**
     * load all plugins.
     *
     * @return plugin definition list.
     */
    public List<AbstractClassEnhancePluginDefine> loadPlugins() {
        PluginResourcesResolver resolver = new PluginResourcesResolver();
        List<URL> resources = resolver.getResources();

        if (resources == null || resources.size() == 0) {
            logger.info("no plugin files (tracing-plugin.def) found, continue to start application.");
            return new ArrayList<AbstractClassEnhancePluginDefine>();
        }

        for (URL pluginUrl : resources) {
            try {
                PluginCfg.INSTANCE.load(pluginUrl.openStream());
            } catch (Throwable t) {
                logger.error("{} plugin file [{}] init failure.", t.getMessage(), pluginUrl);
            }
        }

        List<PluginDefine> pluginClassList = PluginCfg.INSTANCE.getPluginClassList();

        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<AbstractClassEnhancePluginDefine>();
        for (PluginDefine pluginDefine : pluginClassList) {
            try {
                logger.debug("loading plugin class {}.", pluginDefine.getDefineClass());
                AbstractClassEnhancePluginDefine plugin =
                    (AbstractClassEnhancePluginDefine) Class.forName(pluginDefine.getDefineClass()).newInstance();
                plugins.add(plugin);
            } catch (Throwable t) {
                logger.error("{} load plugin [{}] failure.", t.getMessage(), pluginDefine.getDefineClass());
            }
        }
        return plugins;
    }

    /**
     * spiLoad
     * @return
     */
    public List<EnhancePluginDefine> spiLoad(){
        List<EnhancePluginDefine> pluginDefines = new ArrayList<EnhancePluginDefine>();
        Iterator<EnhancePluginDefine> iterator = ServiceLoader.load(EnhancePluginDefine.class).iterator();
        while (iterator.hasNext()){
            pluginDefines.add(iterator.next());
        }
        return pluginDefines;
    }

}
