package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed before each individual test in the enclosing suite.
 *
 * <p>Use this annotation for per-test setup such as resetting mutable state, spawning
 * fresh entities, or clearing inventories. The annotated method is invoked once before
 * every {@link HytaleTest} method in the suite, after the {@link BeforeAll} method
 * (if any) has already run.</p>
 *
 * <pre>{@code
 * @HytaleSuite("Entity Tests")
 * public class EntityTests {
 *
 *     @BeforeEach
 *     void resetWorld() {
 *         TestWorld.clearEntities();
 *     }
 *
 *     @HytaleTest
 *     void testEntitySpawn() { }
 * }
 * }</pre>
 *
 * @see AfterEach
 * @see BeforeAll
 * @see HytaleSuite
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeEach {
}
