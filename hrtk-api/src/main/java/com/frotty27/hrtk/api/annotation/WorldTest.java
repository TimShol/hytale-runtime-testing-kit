package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that combines {@link HytaleTest}, {@link RequiresWorld}, and
 * {@code @Tag("integration")} into a single declaration.
 *
 * <p>Methods annotated with {@code @WorldTest} are automatically scheduled on a world
 * thread and receive {@code WorldTestContext} injection. This is the recommended
 * annotation for any test that needs to interact with blocks, chunks, or world state.</p>
 *
 * <pre>{@code
 * @WorldTest
 * void testBlockPlacement(WorldTestContext ctx) {
 *     ctx.getWorld().setBlock(0, 64, 0, Blocks.STONE);
 *     HytaleAssert.assertEquals(Blocks.STONE, ctx.getWorld().getBlock(0, 64, 0));
 * }
 *
 * @WorldTest(world = "arena_01")
 * void testArenaSetup(WorldTestContext ctx) {
 *     HytaleAssert.assertEquals("arena_01", ctx.getWorld().getName());
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see RequiresWorld
 * @see EcsTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WorldTest {

    /**
     * Name of the world to use. When empty, the default test world is selected.
     *
     * @return the world name, or empty string for the default test world
     */
    String world() default "";
}
