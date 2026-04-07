package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that combines {@link HytaleTest} and {@code @Tag("stats")}
 * into a single declaration for entity stat tests.
 *
 * <p>Methods annotated with {@code @StatsTest} indicate that the test exercises
 * entity stat mechanics such as health, stamina, stat modifiers, and stat boundary
 * validation. This is the recommended annotation for any test that verifies numerical
 * attribute behavior on entities.</p>
 *
 * <pre>{@code
 * @StatsTest
 * void testHealthBounds() {
 *     Entity entity = spawnEntity("player");
 *     entity.setStat(Stats.HEALTH, 999);
 *     HytaleAssert.assertEquals(entity.getMaxHealth(), entity.getHealth());
 * }
 *
 * @StatsTest
 * void testStaminaModifier() {
 *     Entity entity = spawnEntity("player");
 *     entity.addModifier(Stats.STAMINA, Modifier.flat(10));
 *     HytaleAssert.assertEquals(110, entity.getStat(Stats.STAMINA));
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see CombatTest
 * @see EcsTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StatsTest {
}
