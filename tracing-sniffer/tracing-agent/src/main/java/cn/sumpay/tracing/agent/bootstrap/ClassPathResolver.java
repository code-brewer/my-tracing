package cn.sumpay.tracing.agent.bootstrap;

/**
 * @author heyc
 */
public interface ClassPathResolver {

    boolean verify();

    void addJarBaseName(String jarBaseName);

    BootstrapJarFile getBootstrapJarFile();

}
