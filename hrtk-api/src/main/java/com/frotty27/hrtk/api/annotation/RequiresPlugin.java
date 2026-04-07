package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a test method depends on another plugin being loaded on the server.
 *
 * <p>If the specified plugin is not present at runtime, the test is automatically
 * skipped with a {@code SKIPPED} status rather than failing. This is useful for
 * integration tests that exercise cross-plugin interactions.</p>
 *
 * <pre>{@code
 * @HytaleTest
 * @RequiresPlugin("MyOtherMod")
 * void testCrossPluginIntegration() {
 *     HytaleAssert.assertTrue(true);
 * }
 * }</pre>
 *
 * @see RequiresWorld
 * @see RequiresPlayer
 * @see HytaleTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPlugin {

    /**
     * The name of the required plugin (e.g. {@code "MyOtherMod"}).
     *
     * @return the plugin name
     */
    String value();
}
