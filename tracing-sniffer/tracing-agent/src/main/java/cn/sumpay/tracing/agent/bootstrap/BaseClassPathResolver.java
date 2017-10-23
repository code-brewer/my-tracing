package cn.sumpay.tracing.agent.bootstrap;

import cn.sumpay.tracing.agent.core.logger.BootLogger;
import cn.sumpay.tracing.util.StringUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author heyc
 * @date 2017/10/20 14:18
 */
public class BaseClassPathResolver implements ClassPathResolver{

    private BootLogger logger = BootLogger.getLogger(BaseClassPathResolver.class);

    static final String VERSION_PATTERN = "(-[0-9]+(\\.[0-9])+((-SNAPSHOT)|(-RELEASE)|(-RC[0-9]+))?)?";

    private String[] classPaths;

    private Set<String> baseJarSet;

    private BootstrapJarFile bootstrapJarFile = new BootstrapJarFile();

    public BaseClassPathResolver(){
        this(getClassPathFromSystemProperty().split(File.pathSeparator));
    }

    public BaseClassPathResolver(String[] classPaths){
        this.classPaths = classPaths;
    }

    public static String getClassPathFromSystemProperty() {
        return System.getProperty("java.class.path");
    }

    @Override
    public boolean verify() {
        if (baseJarSet == null || baseJarSet.isEmpty()){
            return true;
        }
        try {
            Iterator<String> iterator = baseJarSet.iterator();
            while (iterator.hasNext()){
                String fullJarPath = getFullJarPath(iterator.next());
                if (!StringUtil.isEmpty(fullJarPath)){
                    bootstrapJarFile.append(new JarFile(fullJarPath));
                }
            }
            return true;
        }catch (Exception e){
            logger.warn("verify error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void addJarBaseName(String jarBaseName) {
        if (StringUtil.isEmpty(jarBaseName)){
            return;
        }
        synchronized (this){
            if (baseJarSet == null){
                baseJarSet = new HashSet<String>();
            }
            baseJarSet.add(jarBaseName);
        }
    }

    @Override
    public BootstrapJarFile getBootstrapJarFile() {
        return bootstrapJarFile;
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
                    File file = new File(path);
                    File[] files = file.listFiles();
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
