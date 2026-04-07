package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs a test method once for each set of arguments provided by a {@link ValueSource}.
 *
 * <p>This annotation implies {@link HytaleTest} - there is no need to apply both.
 * Each invocation receives a different argument value and is reported as a separate
 * test result. The display name for each invocation can be customized via the
 * {@link #name()} pattern.</p>
 *
 * <pre>{@code
 * @ParameterizedTest
 * @ValueSource(ints = {1, 5, 10})
 * void testTierScaling(int tier) {
 *     HytaleAssert.assertTrue(tier > 0);
 * }
 *
 * @ParameterizedTest(name = "name={0}")
 * @ValueSource(strings = {"Kweebec_Sapling", "Trork", "Feran"})
 * void testRaceExists(String race) {
 *     HytaleAssert.assertNotNull(RaceRegistry.get(race));
 * }
 * }</pre>
 *
 * @see ValueSource
 * @see HytaleTest
 * @see RepeatedTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParameterizedTest {

    /**
     * Display name pattern for each parameterized invocation. Use {@code {index}} for
     * the zero-based invocation index and {@code {0}} for the argument value.
     *
     * @return the name pattern
     */
    String name() default "[{index}] {0}";
}
