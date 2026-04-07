package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables a test method or an entire test suite so it is skipped during execution.
 *
 * <p>Disabled tests are still discovered by the runner and appear in reports with a
 * {@code SKIPPED} status. An optional reason string can be provided to document why
 * the test is disabled.</p>
 *
 * <pre>{@code
 * @Disabled("Blocked by issue #42")
 * @HytaleTest
 * void testBrokenFeature() { }
 *
 * @Disabled
 * @HytaleSuite("Legacy Tests")
 * public class LegacyTests { }
 * }</pre>
 *
 * @see HytaleTest
 * @see HytaleSuite
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Disabled {

    /**
     * Optional reason explaining why this test or suite is disabled.
     *
     * @return the reason string, or empty string if no reason is provided
     */
    String value() default "";
}
