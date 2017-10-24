package cn.sumpay.tracing.agent.bootstrap;

import java.util.jar.JarFile;

/**
 * @author heyc
 */
public interface ClassPathResolver {

    JarFile getJarFile(String jarName);

}
