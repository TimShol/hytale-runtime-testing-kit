package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns one or more tags to a test method or test suite for selective execution.
 *
 * <p>Tags allow filtering which tests to run via the {@code --tag} flag on the
 * command line. A test is included if it matches any of the requested tags.
 * Tags can be applied at both the class level and the method level.</p>
 *
 * <pre>{@code
 * @Tag("unit")
 * @HytaleTest
 * void testFastPath() { }
 *
 * @Tag({"integration", "ecs"})
 * @HytaleTest
 * void testEcsIntegration() { }
 * }</pre>
 *
 * @see HytaleTest
 * @see HytaleSuite
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Tag {

    /**
     * One or more tag names used to categorize this test or suite.
     *
     * @return the array of tag names
     */
    String[] value();
}
