package com.frotty27.hrtk.api.assert_;

import com.frotty27.hrtk.api.mock.UICommandCapture;

/**
 * Assertions for server-side UI state - pages, commands, and event bindings.
 *
 * <p>Works with {@link UICommandCapture} to verify that UI commands (SET, APPEND,
 * REMOVE, etc.) were issued against the correct element paths with the correct
 * values.</p>
 *
 * <pre>{@code
 * UIAssert.assertCommandSent(capture, "hud.health.bar", "SET");
 * UIAssert.assertCommandSentWithValue(capture, "hud.score.label", 42);
 * UIAssert.assertCommandCount(capture, 3);
 * }</pre>
 *
 * @see UICommandCapture
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class UIAssert {

    private UIAssert() {}

    /**
     * Asserts that a UI command was sent targeting the given path with the given operation.
     *
     * <p>Failure message: {@code "Expected UI command [op] on path 'path' but it was not
     * found (N commands captured)"}</p>
     *
     * @param capture   the captured UI commands
     * @param path      the UI element path (selector)
     * @param operation the operation type (e.g., {@code "SET"}, {@code "APPEND"}, {@code "REMOVE"})
     * @throws AssertionFailedException if no matching command was found
     */
    public static void assertCommandSent(UICommandCapture capture, String path, String operation) {
        if (!capture.hasCommand(path, operation)) {
            HytaleAssert.fail("Expected UI command [%s] on path '%s' but it was not found (%d commands captured)",
                    operation, path, capture.getCount());
        }
    }

    /**
     * Asserts that a SET command was sent on the given path with the given value.
     *
     * <p>Failure message: {@code "Expected UI SET on path 'path' with value <value> but it
     * was not found"}</p>
     *
     * @param capture       the captured UI commands
     * @param path          the UI element path (selector)
     * @param expectedValue the expected value that was set
     * @throws AssertionFailedException if no matching SET command was found
     */
    public static void assertCommandSentWithValue(UICommandCapture capture, String path, Object expectedValue) {
        if (!capture.hasSet(path, expectedValue)) {
            HytaleAssert.fail("Expected UI SET on path '%s' with value <%s> but it was not found",
                    path, expectedValue);
        }
    }

    /**
     * Asserts that an APPEND command was sent on the given page path.
     *
     * <p>Failure message: {@code "Expected UI APPEND at path 'path' but it was not found"}</p>
     *
     * @param capture  the captured UI commands
     * @param pagePath the UI element path where an append is expected
     * @throws AssertionFailedException if no APPEND command was found at the path
     */
    public static void assertPageAppended(UICommandCapture capture, String pagePath) {
        if (!capture.hasCommand(pagePath, "APPEND")) {
            HytaleAssert.fail("Expected UI APPEND at path '%s' but it was not found", pagePath);
        }
    }

    /**
     * Asserts that the capture contains at least one UI command.
     *
     * <p>Failure message: {@code "Expected at least one UI command but none were captured"}</p>
     *
     * @param capture the captured UI commands
     * @throws AssertionFailedException if no commands were captured
     */
    public static void assertHasCommands(UICommandCapture capture) {
        if (capture.getCount() == 0) {
            HytaleAssert.fail("Expected at least one UI command but none were captured");
        }
    }

    /**
     * Asserts that the capture contains exactly the expected number of commands.
     *
     * <p>Failure message: {@code "Expected N UI commands but captured M"}</p>
     *
     * @param capture  the captured UI commands
     * @param expected the exact number of commands expected
     * @throws AssertionFailedException if the count does not match
     */
    public static void assertCommandCount(UICommandCapture capture, int expected) {
        int actual = capture.getCount();
        if (actual != expected) {
            HytaleAssert.fail("Expected %d UI commands but captured %d", expected, actual);
        }
    }
}
