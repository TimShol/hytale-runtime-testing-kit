package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Declares a maximum duration for a test method. If the test exceeds this duration,
 * it is terminated and reported as {@code TIMED_OUT}.
 *
 * <p>This is useful for guarding against infinite loops or unexpectedly slow operations.
 * The default time unit is seconds, but any {@link TimeUnit} can be specified.</p>
 *
 * <pre>{@code
 * @HytaleTest
 * @Timeout(5)
 * void testMustFinishQuickly() { }
 *
 * @HytaleTest
 * @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
 * void testWithMillisecondPrecision() { }
 * }</pre>
 *
 * @see HytaleTest
 * @see AsyncTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timeout {

    /**
     * The maximum duration value. Interpreted according to {@link #unit()}.
     *
     * @return the timeout duration
     */
    long value();

    /**
     * The time unit for the {@link #value()}. Defaults to {@link TimeUnit#SECONDS}.
     *
     * @return the time unit
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
