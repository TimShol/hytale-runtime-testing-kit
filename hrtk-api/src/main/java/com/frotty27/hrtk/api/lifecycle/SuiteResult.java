package com.frotty27.hrtk.api.lifecycle;

import java.util.List;

/**
 * Aggregated results of a test suite execution.
 *
 * <p>Contains all individual {@link TestResult} entries for the suite, along with
 * summary statistics (passed, failed, skipped counts) and total duration.</p>
 *
 * <pre>{@code
 * SuiteResult suite = ...;
 * logger.info("[%s] %d passed, %d failed, %d skipped in %dms",
 *     suite.getSuiteName(), suite.countPassed(), suite.countFailed(),
 *     suite.countSkipped(), suite.getTotalDurationMs());
 * }</pre>
 *
 * @see TestResult
 * @see TestRunListener
 * @since 1.0.0
 */
public final class SuiteResult {

    private final String pluginName;
    private final String suiteName;
    private final List<TestResult> results;
    private final long totalDurationMs;

    /**
     * Constructs a new suite result with all fields.
     *
     * @param pluginName      the name of the plugin that owns the suite
     * @param suiteName       the name of the test suite
     * @param results         the list of individual test results
     * @param totalDurationMs the total suite execution duration in milliseconds
     */
    public SuiteResult(String pluginName, String suiteName, List<TestResult> results, long totalDurationMs) {
        this.pluginName = pluginName;
        this.suiteName = suiteName;
        this.results = List.copyOf(results);
        this.totalDurationMs = totalDurationMs;
    }

    /**
     * Returns the name of the plugin that owns this suite.
     *
     * @return the plugin name
     */
    public String getPluginName() { return pluginName; }

    /**
     * Returns the name of the test suite.
     *
     * @return the suite name
     */
    public String getSuiteName() { return suiteName; }

    /**
     * Returns an unmodifiable list of all individual test results in this suite.
     *
     * @return the list of test results, never {@code null}
     */
    public List<TestResult> getResults() { return results; }

    /**
     * Returns the total suite execution duration in milliseconds.
     *
     * @return the total duration in milliseconds
     */
    public long getTotalDurationMs() { return totalDurationMs; }

    /**
     * Counts the number of tests that passed in this suite.
     *
     * @return the number of passed tests
     */
    public long countPassed() { return results.stream().filter(TestResult::passed).count(); }

    /**
     * Counts the number of tests that failed or errored in this suite.
     *
     * @return the number of failed or errored tests
     */
    public long countFailed() { return results.stream().filter(TestResult::failed).count(); }

    /**
     * Counts the number of tests that were skipped in this suite.
     *
     * @return the number of skipped tests
     */
    public long countSkipped() { return results.stream().filter(r -> r.getStatus() == TestStatus.SKIPPED).count(); }
}
