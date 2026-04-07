package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that combines {@link HytaleTest} and {@code @Tag("spawn")}
 * into a single declaration for entity spawning and despawning tests.
 *
 * <p>Methods annotated with {@code @SpawnTest} indicate that the test exercises
 * entity lifecycle operations such as spawning, despawning, spawn conditions, and
 * entity removal. A world context is available for tests that need to verify spatial
 * spawn behavior.</p>
 *
 * <pre>{@code
 * @SpawnTest
 * void testEntitySpawn() {
 *     Entity entity = EntityFactory.spawn("kweebec", 0, 64, 0);
 *     HytaleAssert.assertNotNull(entity);
 *     HytaleAssert.assertTrue(entity.isAlive());
 * }
 *
 * @SpawnTest(world = "spawn_arena")
 * void testDespawn(WorldTestContext ctx) {
 *     Entity entity = EntityFactory.spawn("trork", 0, 64, 0);
 *     entity.despawn();
 *     HytaleAssert.assertFalse(entity.isAlive());
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see WorldTest
 * @see EcsTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpawnTest {

    /**
     * Name of the world to use. When empty, the default test world is selected.
     *
     * @return the world name, or empty string for the default test world
     */
    String world() default "";
}
