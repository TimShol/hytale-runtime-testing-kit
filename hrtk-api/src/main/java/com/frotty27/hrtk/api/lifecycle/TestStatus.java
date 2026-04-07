package com.frotty27.hrtk.api.lifecycle;

/**
 * Represents the outcome of a single test method execution.
 *
 * <pre>{@code
 * if (result.getStatus() == TestStatus.FAILED) {
 *     logger.error("Test failed: " + result.getMessage());
 * }
 * }</pre>
 *
 * @see TestResult
 * @since 1.0.0
 */
public enum TestStatus {

    /**
     * The test completed successfully with all assertions passing.
     */
    PASSED,

    /**
     * The test failed due to an assertion failure.
     *
     * @see com.frotty27.hrtk.api.assert_.AssertionFailedException
     */
    FAILED,

    /**
     * The test terminated due to an unexpected exception (not an assertion failure).
     */
    ERRORED,

    /**
     * The test was skipped and did not execute.
     */
    SKIPPED,

    /**
     * The test exceeded its configured timeout and was forcibly terminated.
     */
    TIMED_OUT
}
