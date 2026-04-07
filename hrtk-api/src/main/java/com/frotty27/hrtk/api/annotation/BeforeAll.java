package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed once before all tests in the enclosing suite.
 *
 * <p>Use this annotation for expensive one-time setup such as loading fixtures,
 * initializing shared resources, or preparing world state. The annotated method
 * is invoked exactly once per suite execution, before any {@link BeforeEach} or
 * {@link HytaleTest} methods run.</p>
 *
 * <pre>{@code
 * @HytaleSuite("Block Tests")
 * public class BlockTests {
 *
 *     @BeforeAll
 *     void loadFixtures() {
 *         FixtureLoader.load("blocks.json");
 *     }
 *
 *     @HytaleTest
 *     void testBlockHardness() { }
 * }
 * }</pre>
 *
 * @see AfterAll
 * @see BeforeEach
 * @see HytaleSuite
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeAll {
}
