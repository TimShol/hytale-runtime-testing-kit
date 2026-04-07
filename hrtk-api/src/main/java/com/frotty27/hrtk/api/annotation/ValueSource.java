package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides argument values for a {@link ParameterizedTest} method.
 *
 * <p>Exactly one array field should be populated. The test method is invoked once for
 * each element in the provided array, with the element passed as the method parameter.</p>
 *
 * <pre>{@code
 * @ParameterizedTest
 * @ValueSource(ints = {1, 5, 10})
 * void testTierScaling(int tier) {
 *     HytaleAssert.assertTrue(tier > 0);
 * }
 *
 * @ParameterizedTest
 * @ValueSource(strings = {"Kweebec_Sapling", "Trork", "Feran"})
 * void testRaceNames(String race) {
 *     HytaleAssert.assertNotNull(race);
 * }
 * }</pre>
 *
 * @see ParameterizedTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValueSource {

    /**
     * Array of {@code int} arguments to pass to the parameterized test.
     *
     * @return the int argument values
     */
    int[] ints() default {};

    /**
     * Array of {@code long} arguments to pass to the parameterized test.
     *
     * @return the long argument values
     */
    long[] longs() default {};

    /**
     * Array of {@code double} arguments to pass to the parameterized test.
     *
     * @return the double argument values
     */
    double[] doubles() default {};

    /**
     * Array of {@code float} arguments to pass to the parameterized test.
     *
     * @return the float argument values
     */
    float[] floats() default {};

    /**
     * Array of {@link String} arguments to pass to the parameterized test.
     *
     * @return the string argument values
     */
    String[] strings() default {};

    /**
     * Array of {@code boolean} arguments to pass to the parameterized test.
     *
     * @return the boolean argument values
     */
    boolean[] booleans() default {};
}
