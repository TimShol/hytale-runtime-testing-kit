package com.frotty27.hrtk.api.lifecycle;

import java.util.List;

/**
 * Immutable result of a single test method execution.
 *
 * <p>Contains the full context of the test run including identification
 * (plugin, suite, test name), outcome ({@link TestStatus}), timing, failure
 * details, and tags.</p>
 *
 * <pre>{@code
 * TestResult result = ...;
 * if (result.failed()) {
 *     logger.error("[%s] %s: %s", result.getSuiteName(), result.getTestName(), result.getMessage());
 * }
 * }</pre>
 *
 * @see TestStatus
 * @see SuiteResult
 * @since 1.0.0
 */
public final class TestResult {

    private final String pluginName;
    private final String suiteName;
    private final String testName;
    private final String displayName;
    private final TestStatus status;
    private final long durationMs;
    private final String message;
    private final String stackTrace;
    private final List<String> tags;

    /**
     * Constructs a new test result with all fields.
     *
     * @param pluginName  the name of the plugin that owns the test
     * @param suiteName   the name of the test suite
     * @param testName    the test method name
     * @param displayName the human-readable display name
     * @param status      the test outcome
     * @param durationMs  the execution duration in milliseconds
     * @param message     a descriptive message (typically the failure reason), or {@code null}
     * @param stackTrace  the stack trace on failure or error, or {@code null}
     * @param tags        a list of tags associated with the test, or {@code null}
     */
    public TestResult(String pluginName, String suiteName, String testName, String displayName,
                      TestStatus status, long durationMs, String message, String stackTrace,
                      List<String> tags) {
        this.pluginName = pluginName;
        this.suiteName = suiteName;
        this.testName = testName;
        this.displayName = displayName;
        this.status = status;
        this.durationMs = durationMs;
        this.message = message;
        this.stackTrace = stackTrace;
        this.tags = tags != null ? List.copyOf(tags) : List.of();
    }

    /**
     * Returns the name of the plugin that owns this test.
     *
     * @return the plugin name
     */
    public String getPluginName() { return pluginName; }

    /**
     * Returns the name of the test suite containing this test.
     *
     * @return the suite name
     */
    public String getSuiteName() { return suiteName; }

    /**
     * Returns the test method name.
     *
     * @return the test method name
     */
    public String getTestName() { return testName; }

    /**
     * Returns the human-readable display name for this test.
     *
     * @return the display name
     */
    public String getDisplayName() { return displayName; }

    /**
     * Returns the outcome of the test execution.
     *
     * @return the test status
     */
    public TestStatus getStatus() { return status; }

    /**
     * Returns the execution duration in milliseconds.
     *
     * @return the duration in milliseconds
     */
    public long getDurationMs() { return durationMs; }

    /**
     * Returns the descriptive message, typically the failure or error reason.
     *
     * @return the message, or {@code null} if the test passed
     */
    public String getMessage() { return message; }

    /**
     * Returns the stack trace from a failure or error.
     *
     * @return the stack trace string, or {@code null} if the test passed
     */
    public String getStackTrace() { return stackTrace; }

    /**
     * Returns the tags associated with this test.
     *
     * @return an unmodifiable list of tag strings, never {@code null}
     */
    public List<String> getTags() { return tags; }

    /**
     * Returns whether the test passed.
     *
     * @return {@code true} if the status is {@link TestStatus#PASSED}
     */
    public boolean passed() { return status == TestStatus.PASSED; }

    /**
     * Returns whether the test failed or errored.
     *
     * @return {@code true} if the status is {@link TestStatus#FAILED} or {@link TestStatus#ERRORED}
     */
    public boolean failed() { return status == TestStatus.FAILED || status == TestStatus.ERRORED; }
}
