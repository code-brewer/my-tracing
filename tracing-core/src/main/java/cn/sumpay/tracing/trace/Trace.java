package cn.sumpay.tracing.trace;

import java.lang.annotation.*;

/**
 * @author heyc
 */
@Target({ElementType.PARAMETER, ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Trace {

    boolean entry() default false;

    boolean exit() default false;

    String operationName() default "";

    boolean request() default false;

    boolean response() default false;
}
