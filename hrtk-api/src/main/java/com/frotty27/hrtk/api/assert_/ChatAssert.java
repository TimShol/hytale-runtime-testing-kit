package com.frotty27.hrtk.api.assert_;

/**
 * Assertions for chat messages - content checks on captured message strings.
 *
 * <p>Operates on raw message strings rather than Hytale types, so no reflection
 * is needed. Useful for validating messages intercepted by test hooks.</p>
 *
 * <pre>{@code
 * ChatAssert.assertMessageContains(capturedMessage, "Welcome");
 * ChatAssert.assertMessageNotEmpty(capturedMessage);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class ChatAssert {

    private ChatAssert() {}

    /**
     * Asserts that a captured message contains the expected substring.
     *
     * <p>Failure message: {@code "Expected message to contain '<expected>' but was '<message>'"}</p>
     *
     * @param message  the captured message string to check
     * @param expected the substring that should be present in the message
     * @throws IllegalArgumentException    if message or expected is null
     * @throws AssertionFailedException    if the message does not contain the expected substring
     */
    public static void assertMessageContains(String message, String expected) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        if (expected == null) {
            throw new IllegalArgumentException("expected must not be null");
        }
        if (!message.contains(expected)) {
            HytaleAssert.fail("Expected message to contain '%s' but was '%s'", expected, message);
        }
    }

    /**
     * Asserts that a captured message is not null and not empty.
     *
     * <p>Failure message: {@code "Expected message to be non-empty but it was null"} or
     * {@code "Expected message to be non-empty but it was empty"}</p>
     *
     * @param message the captured message string to check
     * @throws IllegalArgumentException    if message is null
     * @throws AssertionFailedException    if the message is empty
     */
    public static void assertMessageNotEmpty(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        if (message.isEmpty()) {
            HytaleAssert.fail("Expected message to be non-empty but it was empty");
        }
    }
}
