package com.frotty27.hrtk.api.lifecycle;

/**
 * Listener for test execution lifecycle events.
 *
 * <p>Implement this interface to receive live progress updates during a test run.
 * All methods have default no-op implementations, so you only need to override
 * the callbacks you are interested in.</p>
 *
 * <pre>{@code
 * public class MyListener implements TestRunListener {
 *     @Override
 *     public void onTestCompleted(TestResult result) {
 *         if (result.failed()) {
 *             logger.warn("FAIL: " + result.getTestName());
 *         }
 *     }
 *
 *     @Override
 *     public void onRunCompleted(List<SuiteResult> results, long totalDurationMs) {
 *         logger.info("All tests completed in " + totalDurationMs + "ms");
 *     }
 * }
 * }</pre>
 *
 * @see TestResult
 * @see SuiteResult
 * @since 1.0.0
 */
public interface TestRunListener {

    /**
     * Called when the entire test run begins.
     *
     * @param totalTests the total number of tests that will be executed
     */
    default void onRunStarted(int totalTests) {}

    /**
     * Called when a test suite begins execution.
     *
     * @param pluginName the name of the plugin owning the suite
     * @param suiteName  the name of the test suite
     * @param testCount  the number of tests in the suite
     */
    default void onSuiteStarted(String pluginName, String suiteName, int testCount) {}

    /**
     * Called when an individual test method begins execution.
     *
     * @param pluginName the name of the plugin owning the test
     * @param suiteName  the name of the test suite
     * @param testName   the name of the test method
     */
    default void onTestStarted(String pluginName, String suiteName, String testName) {}

    /**
     * Called when an individual test method completes (regardless of outcome).
     *
     * @param result the test result containing status, duration, and failure details
     */
    default void onTestCompleted(TestResult result) {}

    /**
     * Called when a test suite finishes execution.
     *
     * @param result the suite result containing all individual test results
     */
    default void onSuiteCompleted(SuiteResult result) {}

    /**
     * Called when the entire test run completes.
     *
     * @param results         the list of all suite results from the run
     * @param totalDurationMs the total run duration in milliseconds
     */
    default void onRunCompleted(java.util.List<SuiteResult> results, long totalDurationMs) {}
}
