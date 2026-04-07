package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns a human-readable display name to a test method for use in reports and logs.
 *
 * <p>This provides an alternative to the {@code value} parameter of {@link HytaleTest}.
 * When both are present, {@code @DisplayName} takes precedence. Use this annotation
 * when a descriptive name would improve readability in test output.</p>
 *
 * <pre>{@code
 * @HytaleTest
 * @DisplayName("Player health should not exceed maximum")
 * void testHealthCap() {
 *     HytaleAssert.assertTrue(player.getHealth() <= player.getMaxHealth());
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DisplayName {

    /**
     * The display name to use for this test in reports and logs.
     *
     * @return the display name
     */
    String value();
}
