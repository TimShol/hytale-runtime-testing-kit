package com.frotty27.hrtk.api.lifecycle;

/**
 * Controls how test state is isolated from the live server environment.
 *
 * <p>The isolation strategy determines what happens to server state before and
 * after a test suite runs. Choose the strategy based on whether your tests
 * read or mutate ECS state, blocks, or world data.</p>
 *
 * <pre>{@code
 * @TestSuite(isolation = IsolationStrategy.SNAPSHOT)
 * public class MyComponentTests { ... }
 * }</pre>
 *
 * @see com.frotty27.hrtk.api.lifecycle.SuiteResult
 * @since 1.0.0
 */
public enum IsolationStrategy {

    /**
     * No isolation. Tests run against live server state.
     *
     * <p>Best for read-only tests and pure logic tests that do not mutate
     * any server state.</p>
     */
    NONE,

    /**
     * ECS state is snapshot before the suite and restored after.
     *
     * <p>Best for tests that mutate component data and need a clean slate
     * restored after execution.</p>
     */
    SNAPSHOT,

    /**
     * A temporary void world is created for the suite and destroyed after.
     *
     * <p>Best for tests that place or destroy blocks, spawn entities, or
     * otherwise require an isolated world environment.</p>
     */
    DEDICATED_WORLD
}
