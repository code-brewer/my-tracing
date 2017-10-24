package cn.sumpay.tracing.agent.bootstrap;

import java.lang.instrument.Instrumentation;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author heyc
 * @date 2017/10/23 21:23
 */
public class BootstrapJarLoader {

    /**
     * instrument
     * @param instrumentation
     * @param bootstrapJars
     * @return
     */
    public static boolean instrument(Instrumentation instrumentation,Set<String> bootstrapJars){
        ClassPathResolver classPathResolver = new BaseClassPathResolver();
        for (String bootstrapJar : bootstrapJars){
            JarFile jarFile = classPathResolver.getJarFile(bootstrapJar);
            if (jarFile != null){
                instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
            }else {
                logPinpointAgentLoadFail();
                return false;
            }
        }
        return true;
    }

    /**
     * logPinpointAgentLoadFail
     */
    private static void logPinpointAgentLoadFail() {
        final String errorLog ="*****************************************************************************\n" +
                                "* Pinpoint Agent load failure\n" +
                                "*****************************************************************************";
        System.err.println(errorLog);
    }
}
