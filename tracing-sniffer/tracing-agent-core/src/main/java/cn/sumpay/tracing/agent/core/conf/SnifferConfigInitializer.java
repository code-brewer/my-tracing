package cn.sumpay.tracing.agent.core.conf;

import cn.sumpay.tracing.util.ConfigInitializer;
import cn.sumpay.tracing.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author heyc
 * @date 2017/10/19 10:53
 */
public class SnifferConfigInitializer {

    private static Logger logger = LoggerFactory.getLogger(SnifferConfigInitializer.class);

    private static String CONFIG_FILE_NAME = "/trace.config";

    /**
     * initialize
     */
    public static void initialize() {

        InputStream configFileStream;

        configFileStream = loadConfigBySystemProperty();

        if (configFileStream == null) {
            configFileStream = SnifferConfigInitializer.class.getResourceAsStream(CONFIG_FILE_NAME);

            if (configFileStream == null) {
                logger.info("No config file found, according system property '-Dconfig'.");
                configFileStream = loadConfigFromAgentFolder();
            } else {
                logger.info("{} file found in class path.", CONFIG_FILE_NAME);
            }
        }

        if (configFileStream == null) {
            logger.info("No {} found, tracing is going to run in default config.", CONFIG_FILE_NAME);
        } else {
            try {
                Properties properties = new Properties();
                properties.load(configFileStream);
                ConfigInitializer.initialize(properties, Config.class);
            } catch (Exception e) {
                logger.error("Failed to read the config file, tracing is going to run in default config.", e);
            }
        }

        String applicationCode = System.getProperty("applicationCode");
        if (!StringUtil.isEmpty(applicationCode)) {
            Config.Agent.APPLICATION_CODE = applicationCode;
        }
        String servers = System.getProperty("servers");
        if (!StringUtil.isEmpty(servers)) {
            Config.Collector.SERVERS = servers;
        }

        if (StringUtil.isEmpty(Config.Agent.APPLICATION_CODE)) {
            throw new ExceptionInInitializerError("'-DapplicationCode=' is missing.");
        }
        if (StringUtil.isEmpty(Config.Collector.SERVERS)) {
            throw new ExceptionInInitializerError("'-Dservers=' is missing.");
        }


    }

    /**
     * loadConfigFromAgentFolder
     * @return
     */
    private static InputStream loadConfigFromAgentFolder() {
        String agentBasePath = initAgentBasePath();
        if (!StringUtil.isEmpty(agentBasePath)) {
            File configFile = new File(agentBasePath, CONFIG_FILE_NAME);
            if (configFile.exists() && configFile.isFile()) {
                try {
                    logger.info("{} file found in agent folder.", CONFIG_FILE_NAME);
                    return new FileInputStream(configFile);
                } catch (FileNotFoundException e) {
                    logger.error(" {} Fail to load {} in path {}, according auto-agent-folder mechanism.",e.getMessage(), CONFIG_FILE_NAME, agentBasePath);
                }
            }
        }
        logger.info("No {} file found in agent folder.", CONFIG_FILE_NAME);
        return null;
    }

    /**
     * loadConfigBySystemProperty
     * @return
     */
    private static InputStream loadConfigBySystemProperty() {
        String config = System.getProperty("config");
        if (StringUtil.isEmpty(config)) {
            return null;
        }
        File configFile = new File(config);
        if (configFile.exists() && configFile.isDirectory()) {
            logger.info("check {} in path {}, according system property.", CONFIG_FILE_NAME, config);
            configFile = new File(config, CONFIG_FILE_NAME);
        }

        if (configFile.exists() && configFile.isFile()) {
            try {
                logger.info("found   {}, according system property.", configFile.getAbsolutePath());
                return new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                logger.error("{} Fail to load {} , according system property.", e.getMessage(),config);
            }
        }
        logger.info("No {}  found, according system property.", config);
        return null;
    }


    /**
     * initAgentBasePath
     * @return
     */
    private static String initAgentBasePath() {
        String classResourcePath = SnifferConfigInitializer.class.getName().replaceAll("\\.", "/") + ".class";

        URL resource = SnifferConfigInitializer.class.getClassLoader().getSystemClassLoader().getResource(classResourcePath);
        if (resource != null) {
            String urlString = resource.toString();
            logger.debug(urlString);
            urlString = urlString.substring(urlString.indexOf("file:"), urlString.indexOf('!'));
            File agentJarFile = null;
            try {
                agentJarFile = new File(new URL(urlString).getFile());
            } catch (MalformedURLException e) {
                logger.error("{} Can not locate agent jar file by url:", e.getMessage(),urlString);
                e.printStackTrace();
            }
            if (agentJarFile.exists()) {
                return agentJarFile.getParentFile().getAbsolutePath();
            }
        }
        logger.info("Can not locate agent jar file.");
        return null;
    }

}
