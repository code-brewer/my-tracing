package cn.sumpay.tracing.agent.core.boot;

/**
 * @author heyc
 */
public interface BootService {

    void beforeBoot() throws Throwable;

    void boot() throws Throwable;

    void afterBoot() throws Throwable;

    void shutdown() throws Throwable;
}
