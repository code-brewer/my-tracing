package cn.sumpay.tracing.agent.core.plugin;

import cn.sumpay.tracing.agent.core.logger.BootLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Use the current classloader to read all plugin define file.
 * The file must be named 'tracing-plugin.def'
 *
 * @author heyc
 */
public class PluginResourcesResolver {
    private static final BootLogger logger = BootLogger.getLogger(PluginResourcesResolver.class);

    public List<URL> getResources() {
        List<URL> cfgUrlPaths = new ArrayList<URL>();
        Enumeration<URL> urls;
        try {
            urls = getDefaultClassLoader().getResources("tracing-plugin.def");
            while (urls.hasMoreElements()) {
                URL pluginUrl = urls.nextElement();
                cfgUrlPaths.add(pluginUrl);
                logger.info("find tracing plugin define in " + pluginUrl);
            }
            return cfgUrlPaths;
        } catch (IOException e) {
            logger.warn("read resources failure.", e);
        }
        return null;
    }

    /**
     * Get the classloader.
     * First get current thread's classloader,
     * if fail, get {@link PluginResourcesResolver}'s classloader.
     *
     * @return the classloader to find plugin definitions.
     */
    private ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = PluginResourcesResolver.class.getClassLoader();
        }
        return cl;
    }

}
