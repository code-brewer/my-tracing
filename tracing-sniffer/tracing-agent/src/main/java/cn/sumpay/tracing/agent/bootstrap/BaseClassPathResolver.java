package cn.sumpay.tracing.agent.bootstrap;

import cn.sumpay.tracing.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author heyc
 * @date 2017/10/20 14:18
 */
public class BaseClassPathResolver implements ClassPathResolver{

    private Logger logger = LoggerFactory.getLogger(BaseClassPathResolver.class);

    static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";

    private String[] classPaths;

    private Set<String> baseJarSet;

    private BootstrapJarFile bootstrapJarFile;

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
            logger.error("verify error: {}",e.getMessage());
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
                if ((jarName + VERSION_PATTERN + ".jar").matches(classPath.substring(classPath.indexOf(jarName)))){
                    return classPath;
                }
            }
        }
        return null;
    }
}
