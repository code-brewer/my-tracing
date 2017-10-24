package cn.sumpay.tracing.agent.core.plugin;

import cn.sumpay.tracing.agent.core.logger.BootLogger;
import cn.sumpay.tracing.agent.core.plugin.interceptor.enhance.EnhancePluginDefine;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author heyc
 */
public class PluginBootstrap {

    private static final BootLogger logger = BootLogger.getLogger(PluginBootstrap.class);

    /**
     * load all plugins.
     *
     * @return plugin definition list.
     */
    public List<EnhancePluginDefine> loadPlugins() {
        PluginResourcesResolver resolver = new PluginResourcesResolver();
        List<URL> resources = resolver.getResources();

        if (resources == null || resources.size() == 0) {
            logger.info("no plugin files (tracing-plugin.def) found, continue to start application.");
            return new ArrayList<EnhancePluginDefine>();
        }

        for (URL pluginUrl : resources) {
            try {
                PluginCfg.INSTANCE.load(pluginUrl.openStream());
            } catch (Throwable t) {
                logger.warn(t.getMessage() + " plugin file [" + pluginUrl + "] init failure.");
            }
        }

        List<PluginDefine> pluginClassList = PluginCfg.INSTANCE.getPluginClassList();

        List<EnhancePluginDefine> plugins = new ArrayList<EnhancePluginDefine>();
        for (PluginDefine pluginDefine : pluginClassList) {
            try {
                logger.info("loading plugin class " + pluginDefine.getDefineClass());
                plugins.add((EnhancePluginDefine) Class.forName(pluginDefine.getDefineClass()).newInstance());
            } catch (Throwable t) {
                logger.warn(t.getMessage() + " load plugin [" + pluginDefine.getDefineClass() + "] failure.");
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
