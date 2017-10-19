package cn.sumpay.tracing.agent.core.plugin.interceptor.enhance;

import cn.sumpay.tracing.agent.core.plugin.AbstractClassEnhancePluginDefine;
import cn.sumpay.tracing.agent.core.plugin.PluginException;
import cn.sumpay.tracing.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import cn.sumpay.tracing.agent.core.plugin.interceptor.EnhanceException;
import cn.sumpay.tracing.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import cn.sumpay.tracing.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import cn.sumpay.tracing.util.StringUtil;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.bytebuddy.jar.asm.Opcodes.ACC_PRIVATE;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * This class controls all enhance operations, including enhance constructors, instance methods and static methods. All
 * the enhances base on three types interceptor point: {@link ConstructorInterceptPoint}, {@link
 * InstanceMethodsInterceptPoint} and {@link StaticMethodsInterceptPoint} If plugin is going to enhance constructors,
 * instance methods, or both, {@link ClassEnhancePluginDefine} will add a field of {@link
 * Object} type.
 *
 * @author heyc
 */
public abstract class ClassEnhancePluginDefine extends AbstractClassEnhancePluginDefine {
    private static final Logger logger = LoggerFactory.getLogger(ClassEnhancePluginDefine.class);

    /**
     * New field name.
     */
    public static final String CONTEXT_ATTR_NAME = "_$EnhancedClassField_ws";

    /**
     * Begin to define how to enhance class.
     * After invoke this method, only means definition is finished.
     *
     * @param enhanceOriginClassName target class name
     * @param newClassBuilder byte-buddy's builder to manipulate class bytecode.
     * @return new byte-buddy's builder for further manipulation.
     */
    @Override
    public DynamicType.Builder<?> enhance(String enhanceOriginClassName,
                                             DynamicType.Builder<?> newClassBuilder, ClassLoader classLoader) throws PluginException {
        newClassBuilder = this.enhanceClass(enhanceOriginClassName, newClassBuilder, classLoader);

        newClassBuilder = this.enhanceInstance(enhanceOriginClassName, newClassBuilder, classLoader);

        return newClassBuilder;
    }

    /**
     * Enhance a class to intercept constructors and class instance methods.
     *
     * @param enhanceOriginClassName target class name
     * @param newClassBuilder byte-buddy's builder to manipulate class bytecode.
     * @return new byte-buddy's builder for further manipulation.
     */
    private DynamicType.Builder<?> enhanceInstance(String enhanceOriginClassName,
                                                   DynamicType.Builder<?> newClassBuilder, ClassLoader classLoader) throws PluginException {
        ConstructorInterceptPoint[] constructorInterceptPoints = getConstructorsInterceptPoints();
        InstanceMethodsInterceptPoint[] instanceMethodsInterceptPoints = getInstanceMethodsInterceptPoints();

        boolean existedConstructorInterceptPoint = false;
        if (constructorInterceptPoints != null && constructorInterceptPoints.length > 0) {
            existedConstructorInterceptPoint = true;
        }
        boolean existedMethodsInterceptPoints = false;
        if (instanceMethodsInterceptPoints != null && instanceMethodsInterceptPoints.length > 0) {
            existedMethodsInterceptPoints = true;
        }

        /**
         * nothing need to be enhanced in class instance, maybe need enhance static methods.
         */
        if (!existedConstructorInterceptPoint && !existedMethodsInterceptPoints) {
            return newClassBuilder;
        }

        /**
         * alter class source code.<br/>
         *
         * new class need:<br/>
         * 1.Add field, name {@link #CONTEXT_ATTR_NAME}.
         * 2.Add a field accessor for this field.
         *
         */
        newClassBuilder = newClassBuilder.defineField(CONTEXT_ATTR_NAME, Object.class, ACC_PRIVATE)
            .implement(EnhancedInstance.class)
            .intercept(FieldAccessor.ofField(CONTEXT_ATTR_NAME));

        /**
         * 2. enhance constructors
         */
        if (existedConstructorInterceptPoint) {
            for (ConstructorInterceptPoint constructorInterceptPoint : constructorInterceptPoints) {
                newClassBuilder = newClassBuilder.constructor(constructorInterceptPoint.getConstructorMatcher()).intercept(SuperMethodCall.INSTANCE
                    .andThen(MethodDelegation.withDefaultConfiguration()
                        .to(new ConstructorInter(constructorInterceptPoint.getConstructorInterceptor(), classLoader))
                    )
                );
            }
        }

        /**
         * 3. enhance instance methods
         */
        if (existedMethodsInterceptPoints) {
            for (InstanceMethodsInterceptPoint instanceMethodsInterceptPoint : instanceMethodsInterceptPoints) {
                String interceptor = instanceMethodsInterceptPoint.getMethodsInterceptor();
                if (StringUtil.isEmpty(interceptor)) {
                    throw new EnhanceException("no InstanceMethodsAroundInterceptor define to enhance class " + enhanceOriginClassName);
                }

                if (instanceMethodsInterceptPoint.isOverrideArgs()) {
                    newClassBuilder =
                        newClassBuilder.method(not(isStatic()).and(instanceMethodsInterceptPoint.getMethodsMatcher()))
                            .intercept(
                                MethodDelegation.withDefaultConfiguration()
                                    .withBinders(
                                        Morph.Binder.install(OverrideCallable.class)
                                    )
                                    .to(new InstMethodsInterWithOverrideArgs(interceptor, classLoader))
                            );
                } else {
                    newClassBuilder =
                        newClassBuilder.method(not(isStatic()).and(instanceMethodsInterceptPoint.getMethodsMatcher()))
                            .intercept(
                                MethodDelegation.withDefaultConfiguration()
                                    .to(new InstMethodsInter(interceptor, classLoader))
                            );
                }
            }
        }

        return newClassBuilder;
    }

    /**
     * Constructor methods intercept point. See {@link ConstructorInterceptPoint}
     *
     * @return collections of {@link ConstructorInterceptPoint}
     */
    protected abstract ConstructorInterceptPoint[] getConstructorsInterceptPoints();

    /**
     * Instance methods intercept point. See {@link InstanceMethodsInterceptPoint}
     *
     * @return collections of {@link InstanceMethodsInterceptPoint}
     */
    protected abstract InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints();

    /**
     * Enhance a class to intercept class static methods.
     *
     * @param enhanceOriginClassName target class name
     * @param newClassBuilder byte-buddy's builder to manipulate class bytecode.
     * @return new byte-buddy's builder for further manipulation.
     */
    private DynamicType.Builder<?> enhanceClass(String enhanceOriginClassName,
                                                DynamicType.Builder<?> newClassBuilder, ClassLoader classLoader) throws PluginException {
        StaticMethodsInterceptPoint[] staticMethodsInterceptPoints = getStaticMethodsInterceptPoints();

        if (staticMethodsInterceptPoints == null || staticMethodsInterceptPoints.length == 0) {
            return newClassBuilder;
        }

        for (StaticMethodsInterceptPoint staticMethodsInterceptPoint : staticMethodsInterceptPoints) {
            String interceptor = staticMethodsInterceptPoint.getMethodsInterceptor();
            if (StringUtil.isEmpty(interceptor)) {
                throw new EnhanceException("no StaticMethodsAroundInterceptor define to enhance class " + enhanceOriginClassName);
            }

            if (staticMethodsInterceptPoint.isOverrideArgs()) {
                newClassBuilder = newClassBuilder.method(isStatic().and(staticMethodsInterceptPoint.getMethodsMatcher()))
                    .intercept(
                        MethodDelegation.withDefaultConfiguration()
                            .withBinders(
                                Morph.Binder.install(OverrideCallable.class)
                            )
                            .to(new StaticMethodsInter(interceptor))
                    );
            } else {
                newClassBuilder = newClassBuilder.method(isStatic().and(staticMethodsInterceptPoint.getMethodsMatcher()))
                    .intercept(
                        MethodDelegation.withDefaultConfiguration()
                            .to(new StaticMethodsInter(interceptor))
                    );
            }

        }

        return newClassBuilder;
    }

    /**
     * Static methods intercept point. See {@link StaticMethodsInterceptPoint}
     *
     * @return collections of {@link StaticMethodsInterceptPoint}
     */
    protected abstract StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints();
}
