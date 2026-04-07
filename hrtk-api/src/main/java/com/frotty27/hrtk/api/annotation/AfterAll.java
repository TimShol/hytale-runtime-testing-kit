package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed once after all tests in the enclosing suite have completed.
 *
 * <p>Use this annotation for one-time teardown logic such as releasing shared resources,
 * cleaning up world state, or flushing logs. The annotated method is invoked exactly
 * once per suite execution, after every {@link HytaleTest} and {@link AfterEach} method
 * has finished.</p>
 *
 * <pre>{@code
 * @HytaleSuite("Block Tests")
 * public class BlockTests {
 *
 *     @AfterAll
 *     void cleanupFixtures() {
 *         FixtureLoader.unloadAll();
 *     }
 *
 *     @HytaleTest
 *     void testBlockHardness() { }
 * }
 * }</pre>
 *
 * @see BeforeAll
 * @see AfterEach
 * @see HytaleSuite
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterAll {
}
