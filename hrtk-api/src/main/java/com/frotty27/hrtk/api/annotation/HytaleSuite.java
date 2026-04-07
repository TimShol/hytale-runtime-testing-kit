package com.frotty27.hrtk.api.annotation;

import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a test suite for grouping related tests and configuring isolation.
 *
 * <p>While optional, applying {@code @HytaleSuite} to a test class is recommended.
 * It provides a human-readable suite name for reporting and allows configuring an
 * {@link IsolationStrategy} to control how tests within the suite share state.</p>
 *
 * <pre>{@code
 * @HytaleSuite("My Tests")
 * public class MyTests {
 *
 *     @HytaleTest
 *     void testBasicMath() {
 *         HytaleAssert.assertEquals(4, 2 + 2);
 *     }
 * }
 *
 * @HytaleSuite(value = "Isolated Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
 * public class WorldTests {
 *
 *     @WorldTest
 *     void testBlockPlacement() { }
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see BeforeAll
 * @see AfterAll
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HytaleSuite {

    /**
     * Display name for this suite. When empty, the simple class name is used.
     *
     * @return the suite display name, or empty string for auto-generated name
     */
    String value() default "";

    /**
     * Isolation strategy that controls how tests within this suite share state.
     *
     * @return the isolation strategy to apply
     */
    IsolationStrategy isolation() default IsolationStrategy.NONE;
}
