package cn.sumpay.tracing.agent.core.boot;

import cn.sumpay.tracing.agent.core.logger.BootLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The <code>ServiceManager</code> bases on {@link ServiceLoader},
 * load all {@link BootService} implementations.
 *
 * @author heyc
 */
public enum ServiceManager {

    INSTANCE;

    private static final BootLogger logger = BootLogger.getLogger(ServiceManager.class);
    private Map<Class, BootService> bootedServices = new HashMap<Class, BootService>();

    public void boot() {
        bootedServices = loadAllServices();

        beforeBoot();
        startup();
        afterBoot();
    }

    public void shutdown() {
        for (BootService service : bootedServices.values()) {
            try {
                service.shutdown();
            } catch (Throwable e) {
                logger.warn(e.getMessage() + " ServiceManager try to shutdown [" +service.getClass().getName()+ "] fail.");
            }
        }
    }

    private Map<Class, BootService> loadAllServices() {
        HashMap<Class, BootService> bootedServices = new HashMap<Class, BootService>();
        Iterator<BootService> serviceIterator = load().iterator();
        while (serviceIterator.hasNext()) {
            BootService bootService = serviceIterator.next();
            bootedServices.put(bootService.getClass(), bootService);
        }
        return bootedServices;
    }

    private void beforeBoot() {
        for (BootService service : bootedServices.values()) {
            try {
                service.beforeBoot();
            } catch (Throwable e) {
                logger.warn(e.getMessage() + " ServiceManager try to pre-start [" +service.getClass().getName()+ "] fail.");
            }
        }
    }

    private void startup() {
        for (BootService service : bootedServices.values()) {
            try {
                service.boot();
            } catch (Throwable e) {
                logger.warn(e.getMessage() + " ServiceManager try to start ["+service.getClass().getName()+"] fail.");
            }
        }
    }

    private void afterBoot() {
        for (BootService service : bootedServices.values()) {
            try {
                service.afterBoot();
            } catch (Throwable e) {
                logger.warn(e.getMessage() + " Service ["+service.getClass().getName()+"] AfterBoot process fails.");
            }
        }
    }

    /**
     * Find a {@link BootService} implementation, which is already started.
     *
     * @param serviceClass class name.
     * @param <T> {@link BootService} implementation class.
     * @return {@link BootService} instance
     */
    public <T extends BootService> T findService(Class<T> serviceClass) {
        return (T)bootedServices.get(serviceClass);
    }

    ServiceLoader<BootService> load() {
        return ServiceLoader.load(BootService.class);
    }
}
