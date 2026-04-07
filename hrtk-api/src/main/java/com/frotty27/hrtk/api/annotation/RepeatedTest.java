package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs a test method a specified number of times. Each repetition is reported
 * as a separate test result.
 *
 * <p>This annotation implies {@link HytaleTest} - there is no need to apply both.
 * It is useful for verifying that behavior is consistent across multiple executions
 * or for stress-testing non-deterministic logic.</p>
 *
 * <pre>{@code
 * @RepeatedTest(5)
 * void testRandomLootDrop() {
 *     Item drop = LootTable.roll("common_chest");
 *     HytaleAssert.assertNotNull(drop);
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see ParameterizedTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RepeatedTest {

    /**
     * The number of times to repeat the test.
     *
     * @return the repetition count (must be greater than zero)
     */
    int value();
}
