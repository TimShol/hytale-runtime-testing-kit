package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a test method requires a world context to execute.
 *
 * <p>The test runner will schedule the annotated method on a world thread and
 * enable {@code WorldTestContext} injection. If a specific world name is provided,
 * that world is used; otherwise the default test world or any available world is
 * selected automatically.</p>
 *
 * <pre>{@code
 * @HytaleTest
 * @RequiresWorld
 * void testDefaultWorld(WorldTestContext ctx) {
 *     HytaleAssert.assertNotNull(ctx.getWorld());
 * }
 *
 * @HytaleTest
 * @RequiresWorld("arena_01")
 * void testSpecificWorld(WorldTestContext ctx) {
 *     HytaleAssert.assertEquals("arena_01", ctx.getWorld().getName());
 * }
 * }</pre>
 *
 * @see WorldTest
 * @see RequiresPlayer
 * @see HytaleTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresWorld {

    /**
     * Name of the world to use. When empty, the default test world or any
     * available world is selected.
     *
     * @return the world name, or empty string for automatic selection
     */
    String value() default "";
}
