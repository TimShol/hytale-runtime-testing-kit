package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test method as asynchronous, allowing it to span multiple server ticks.
 *
 * <p>The test runner will wait up to {@link #timeoutTicks()} server ticks for the
 * test to signal completion. If the test does not complete within the allotted ticks,
 * it is terminated and reported as {@code TIMED_OUT}. This is useful for tests that
 * involve scheduled tasks, delayed events, or multi-tick game logic.</p>
 *
 * <pre>{@code
 * @AsyncTest
 * @HytaleTest
 * void testDelayedEvent(AsyncTestContext ctx) {
 *     EventBus.schedule(() -> ctx.complete(), 10);
 * }
 *
 * @AsyncTest(timeoutTicks = 400)
 * @HytaleTest
 * void testLongRunningProcess(AsyncTestContext ctx) {
 *     SlowProcess.start(() -> ctx.complete());
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see FlowTest
 * @see Timeout
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AsyncTest {

    /**
     * Maximum number of server ticks to wait before the test is considered timed out.
     *
     * @return the timeout in server ticks (defaults to 200)
     */
    int timeoutTicks() default 200;
}
