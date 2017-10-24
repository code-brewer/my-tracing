package cn.sumpay.tracing.agent.bootstrap;

import cn.sumpay.tracing.agent.core.logger.BootLogger;
import cn.sumpay.tracing.util.StringUtil;

import java.io.File;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author heyc
 * @date 2017/10/20 14:18
 */
public class BaseClassPathResolver implements ClassPathResolver{

    private BootLogger logger = BootLogger.getLogger(BaseClassPathResolver.class);

    static final String VERSION_PATTERN = "(-[0-9]+(\\.[0-9])+((-SNAPSHOT)|(-RELEASE)|(-RC[0-9]+))?)?";

    static final String AGENT_JAR_NAME = "tracing-agent";

    private String agentPath;

    private String[] classPaths;

    public BaseClassPathResolver(){
        this(getClassPathFromSystemProperty().split(File.pathSeparator));
    }

    public BaseClassPathResolver(String[] classPaths){
        this.classPaths = classPaths;
        String agentFullPath = getFullJarPath(AGENT_JAR_NAME);
        this.agentPath = agentFullPath.substring(0,agentFullPath.lastIndexOf(AGENT_JAR_NAME));
    }

    public static String getClassPathFromSystemProperty() {
        return System.getProperty("java.class.path");
    }

    @Override
    public JarFile getJarFile(String jarName){
        try {
            String fullJarPath = getFullJarPath(jarName);
            if (!StringUtil.isEmpty(fullJarPath)){
                logger.info("find jarFile :" + fullJarPath);
                return new JarFile(fullJarPath);
            }
            logger.warn("can not find jarFile :" + jarName);
            return null;
        }catch (Exception e){
            logger.warn("verify error: " + e.getMessage());
            return null;
        }
    }

    /**
     *
     * @param jarName
     * @return
     */
    private String getFullJarPath(String jarName) {
        for (String classPath : classPaths) {
            if (classPath.contains(jarName)){
                if (classPath.endsWith("classes")){
                    String path = classPath.substring(0, classPath.lastIndexOf("classes"));
                    File[] files = new File(path).listFiles();
                    for (File f : files){
                        if (Pattern.compile(jarName + VERSION_PATTERN + "\\.jar").matcher(f.getName()).matches()){
                            return path + f.getName();
                        }
                    }
                }else {
                    String fullJarName = classPath.substring(classPath.lastIndexOf(jarName));
                    if (Pattern.compile(jarName + VERSION_PATTERN + "\\.jar").matcher(fullJarName).matches()){
                        return classPath;
                    }
                }
            }
        }
        return null;
    }
}
