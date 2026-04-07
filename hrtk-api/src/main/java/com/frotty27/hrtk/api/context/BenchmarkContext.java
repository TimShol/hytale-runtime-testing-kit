package com.frotty27.hrtk.api.context;

/**
 * Test context for benchmark tests, providing iteration info and manual timer control.
 *
 * <p>Available when using the {@code @Benchmark} annotation. The framework
 * executes the benchmark method multiple times, including warmup iterations
 * that are not measured.</p>
 *
 * <pre>{@code
 * @Benchmark(iterations = 100, warmup = 10)
 * public void benchmarkLookup(BenchmarkContext ctx) {
 *     if (ctx.isWarmup()) {
 *         // warmup - results not recorded
 *     }
 *     ctx.startTimer();
 *     performExpensiveOperation();
 *     ctx.stopTimer();
 * }
 * }</pre>
 *
 * @see TestContext
 * @since 1.0.0
 */
public interface BenchmarkContext extends TestContext {

    /**
     * Returns the current iteration number (0-based).
     *
     * <p>Warmup iterations are numbered separately; this counter only reflects
     * the current phase (warmup or measured).</p>
     *
     * @return the zero-based iteration index
     */
    int getIteration();

    /**
     * Returns the total number of measured iterations (excluding warmup).
     *
     * @return the total number of measured iterations
     */
    int getTotalIterations();

    /**
     * Returns whether the current iteration is a warmup iteration.
     *
     * <p>Warmup iterations are not included in benchmark measurements. Use this
     * to skip expensive setup that should only run during measured iterations.</p>
     *
     * @return {@code true} if this is a warmup iteration, {@code false} if measured
     */
    boolean isWarmup();

    /**
     * Manually starts a timing measurement.
     *
     * <p>By default, the entire method body is timed. Call this method to begin
     * timing at a specific point, paired with {@link #stopTimer()} to end it.
     * This allows excluding setup and teardown from the measurement.</p>
     *
     * @see #stopTimer()
     */
    void startTimer();

    /**
     * Stops the current timing measurement started by {@link #startTimer()}.
     *
     * @see #startTimer()
     */
    void stopTimer();
}
