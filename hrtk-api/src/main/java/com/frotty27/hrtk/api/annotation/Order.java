package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls the execution order of a test method within its enclosing suite.
 *
 * <p>Tests are sorted by their order value in ascending order - lower values run first.
 * Tests without an explicit {@code @Order} annotation are assigned
 * {@link Integer#MAX_VALUE} and execute after all ordered tests.</p>
 *
 * <pre>{@code
 * @Order(1)
 * @HytaleTest
 * void testFirst() { }
 *
 * @Order(2)
 * @HytaleTest
 * void testSecond() { }
 *
 * @HytaleTest
 * void testLast() { }
 * }</pre>
 *
 * @see HytaleTest
 * @see HytaleSuite
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Order {

    /**
     * Execution priority for this test. Lower values execute earlier.
     *
     * @return the order value
     */
    int value();
}
