package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that combines {@link HytaleTest} and {@code @Tag("ecs")}
 * into a single declaration for Entity Component System tests.
 *
 * <p>Methods annotated with {@code @EcsTest} receive {@code EcsTestContext} injection,
 * providing direct access to the ECS {@code Store} and {@code CommandBuffer}. Use this
 * annotation for tests that verify component queries, system behavior, or entity
 * lifecycle operations.</p>
 *
 * <pre>{@code
 * @EcsTest
 * void testComponentAttach(EcsTestContext ctx) {
 *     Entity entity = ctx.getStore().createEntity();
 *     ctx.getCommandBuffer().addComponent(entity, new HealthComponent(100));
 *     ctx.flush();
 *     HytaleAssert.assertTrue(ctx.getStore().hasComponent(entity, HealthComponent.class));
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see WorldTest
 * @see StatsTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EcsTest {
}
