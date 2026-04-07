package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed after each individual test in the enclosing suite.
 *
 * <p>Use this annotation for per-test teardown such as despawning entities, reverting
 * world changes, or resetting mock state. The annotated method is invoked once after
 * every {@link HytaleTest} method in the suite, before the {@link AfterAll} method
 * (if any) runs.</p>
 *
 * <pre>{@code
 * @HytaleSuite("Entity Tests")
 * public class EntityTests {
 *
 *     @AfterEach
 *     void despawnEntities() {
 *         TestWorld.clearEntities();
 *     }
 *
 *     @HytaleTest
 *     void testEntitySpawn() { }
 * }
 * }</pre>
 *
 * @see BeforeEach
 * @see AfterAll
 * @see HytaleSuite
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterEach {
}
