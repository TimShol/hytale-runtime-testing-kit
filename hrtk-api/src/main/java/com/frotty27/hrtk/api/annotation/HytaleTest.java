package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a test case to be discovered and executed by the HRTK test runner.
 *
 * <p>This is the primary test annotation. Methods annotated with {@code @HytaleTest}
 * are discovered during JAR scanning and executed when {@code /hrtk run} is invoked.</p>
 *
 * <pre>{@code
 * @HytaleTest
 * void testSomething() {
 *     HytaleAssert.assertEquals(4, 2 + 2);
 * }
 *
 * @HytaleTest("Custom display name")
 * void testWithName() {
 *     HytaleAssert.assertTrue(true);
 * }
 * }</pre>
 *
 * @see HytaleSuite
 * @see DisplayName
 * @see EcsTest
 * @see WorldTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HytaleTest {

    /**
     * Optional display name for this test. When empty, the method name is used.
     *
     * @return the display name, or empty string for auto-generated name
     */
    String value() default "";
}
