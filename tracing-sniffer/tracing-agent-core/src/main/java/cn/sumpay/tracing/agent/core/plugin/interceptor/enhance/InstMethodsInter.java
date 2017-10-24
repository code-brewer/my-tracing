package cn.sumpay.tracing.agent.core.plugin.interceptor.enhance;

import cn.sumpay.tracing.agent.core.logger.BootLogger;
import cn.sumpay.tracing.agent.core.plugin.PluginException;
import cn.sumpay.tracing.agent.core.plugin.interceptor.loader.InterceptorInstanceLoader;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * The actual byte-buddy's interceptor to intercept class instance methods.
 * In this class, it provide a bridge between byte-buddy and tracing plugin.
 *
 * @author heyc
 */
public class InstMethodsInter {
    private static final BootLogger logger = BootLogger.getLogger(InstMethodsInter.class);

    /**
     * An {@link InstanceMethodsAroundInterceptor}
     * This name should only stay in {@link String}, the real {@link Class} type will trigger classloader failure.
     * If you want to know more, please check on books about Classloader or Classloader appointment mechanism.
     */
    private InstanceMethodsAroundInterceptor interceptor;

    /**
     * @param instanceMethodsAroundInterceptorClassName class full name.
     */
    public InstMethodsInter(String instanceMethodsAroundInterceptorClassName, ClassLoader classLoader) {
        try {
            interceptor = InterceptorInstanceLoader.load(instanceMethodsAroundInterceptorClassName, classLoader);
        } catch (Throwable t) {
            throw new PluginException("Can't create InstanceMethodsAroundInterceptor.", t);
        }
    }

    /**
     * Intercept the target instance method.
     *
     * @param obj target class instance.
     * @param allArguments all method arguments
     * @param method method description.
     * @param zuper the origin call ref.
     * @return the return value of target instance method.
     * @throws Exception only throw exception because of zuper.call() or unexpected exception in sky-walking ( This is a
     * bug, if anything triggers this condition ).
     */
    @RuntimeType
    public Object intercept(@This Object obj,
        @AllArguments Object[] allArguments,
        @SuperCall Callable<?> zuper,
        @Origin Method method
    ) throws Throwable {
        EnhancedInstance targetObject = (EnhancedInstance)obj;

        MethodInterceptResult result = new MethodInterceptResult();
        try {
            interceptor.beforeMethod(targetObject, method, allArguments, method.getParameterTypes(),result);
        } catch (Throwable t) {
            logger.warn(t.getMessage() + " class["+obj.getClass()+"] before method["+method.getName()+"] intercept failure");
            t.printStackTrace();
        }

        Object ret = null;
        try {
            if (!result.isContinue()) {
                ret = result._ret();
            } else {
                ret = zuper.call();
            }
        } catch (Throwable t) {
            try {
                interceptor.handleMethodException(targetObject, method, allArguments, method.getParameterTypes(),t);
            } catch (Throwable t2) {
                logger.warn(t2.getMessage() + " class["+obj.getClass()+"] handle method["+ method.getName()+"] exception failure");
            }
            throw t;
        } finally {
            try {
                ret = interceptor.afterMethod(targetObject, method, allArguments, method.getParameterTypes(),ret);
            } catch (Throwable t) {
                logger.warn(t.getMessage() + " class["+obj.getClass()+"] after method["+method.getName()+"] intercept failure");
            }
        }
        return ret;
    }
}
