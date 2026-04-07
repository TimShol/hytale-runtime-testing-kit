package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that combines {@link HytaleTest} and {@code @Tag("combat")}
 * into a single declaration for combat mechanic tests.
 *
 * <p>Methods annotated with {@code @CombatTest} indicate that the test exercises
 * combat-related systems such as damage calculation, health management, knockback,
 * death handling, or weapon interactions. A world context is available for tests
 * that need spatial positioning.</p>
 *
 * <pre>{@code
 * @CombatTest
 * void testMeleeDamage() {
 *     Entity attacker = spawnEntity("player");
 *     Entity target = spawnEntity("dummy");
 *     CombatSystem.attack(attacker, target);
 *     HytaleAssert.assertTrue(target.getHealth() < target.getMaxHealth());
 * }
 *
 * @CombatTest(world = "arena_01")
 * void testArenaKnockback(WorldTestContext ctx) {
 *     // Test knockback in a specific arena world
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see StatsTest
 * @see WorldTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CombatTest {

    /**
     * Name of the world to use. When empty, the default test world is selected.
     *
     * @return the world name, or empty string for the default test world
     */
    String world() default "";
}
