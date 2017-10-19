package cn.sumpay.tracing.agent.core.plugin;

import cn.sumpay.tracing.agent.core.plugin.exception.IllegalPluginDefineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public enum PluginCfg {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(PluginCfg.class);

    private List<PluginDefine> pluginClassList = new ArrayList<PluginDefine>();

    void load(InputStream input) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String pluginDefine = null;
            while ((pluginDefine = reader.readLine()) != null) {
                try {
                    if (pluginDefine == null || pluginDefine.trim().length() == 0) {
                        continue;
                    }
                    PluginDefine plugin = PluginDefine.build(pluginDefine);
                    if (plugin.enable()) {
                        pluginClassList.add(plugin);
                    }
                } catch (IllegalPluginDefineException e) {
                    logger.error("{} Failed to format plugin({}) define.", e.getMessage(), pluginDefine);
                }
            }
        } finally {
            input.close();
        }
    }

    public List<PluginDefine> getPluginClassList() {
        return pluginClassList;
    }

}
