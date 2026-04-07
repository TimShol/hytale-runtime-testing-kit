package com.frotty27.hrtk.api.assert_;

import com.frotty27.hrtk.api.context.TestContext;
import com.frotty27.hrtk.api.mock.MockCommandSender;

/**
 * Assertions for command execution and output verification.
 *
 * <p>Provides methods to execute commands through a {@link TestContext} and assert
 * their success, failure, failure messages, and sender output. Uses a
 * {@link MockCommandSender} to capture messages sent back to the command issuer.</p>
 *
 * <pre>{@code
 * MockCommandSender sender = ctx.createCommandSender("admin.ban");
 * CommandAssert.assertCommandSucceeds(ctx, sender, "/ban player1");
 * CommandAssert.assertSenderReceivedMessage(sender, "has been banned");
 * }</pre>
 *
 * @see TestContext
 * @see MockCommandSender
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class CommandAssert {

    private CommandAssert() {}

    /**
     * Executes a command and asserts it succeeds (does not throw an exception).
     *
     * <p>Failure message: {@code "Expected command 'cmd' to succeed but it threw: message"}</p>
     *
     * @param ctx         the test context used to execute the command
     * @param sender      the mock command sender to execute as
     * @param commandLine the full command string to execute
     * @throws AssertionFailedException if the command throws an exception
     */
    public static void assertCommandSucceeds(TestContext ctx, MockCommandSender sender, String commandLine) {
        try {
            ctx.executeCommand(commandLine, sender);
        } catch (Exception e) {
            HytaleAssert.fail("Expected command '%s' to succeed but it threw: %s",
                    commandLine, e.getMessage());
        }
    }

    /**
     * Executes a command and asserts it fails (throws an exception).
     *
     * <p>Failure message: {@code "Expected command 'cmd' to fail but it succeeded"}</p>
     *
     * @param ctx         the test context used to execute the command
     * @param sender      the mock command sender to execute as
     * @param commandLine the full command string to execute
     * @throws AssertionFailedException if the command succeeds without throwing
     */
    public static void assertCommandFails(TestContext ctx, MockCommandSender sender, String commandLine) {
        try {
            ctx.executeCommand(commandLine, sender);
            HytaleAssert.fail("Expected command '%s' to fail but it succeeded", commandLine);
        } catch (Exception _) {
        }
    }

    /**
     * Executes a command and asserts it fails with an error message containing the
     * expected substring.
     *
     * <p>Failure message: {@code "Expected command 'cmd' to fail but it succeeded"} or
     * {@code "Command failed but message 'msg' did not contain 'expected'"}</p>
     *
     * @param ctx             the test context used to execute the command
     * @param sender          the mock command sender to execute as
     * @param commandLine     the full command string to execute
     * @param expectedMessage the substring expected in the error message
     * @throws AssertionFailedException if the command succeeds or fails with a non-matching message
     */
    public static void assertCommandFailsWithMessage(TestContext ctx, MockCommandSender sender,
                                                      String commandLine, String expectedMessage) {
        try {
            ctx.executeCommand(commandLine, sender);
            HytaleAssert.fail("Expected command '%s' to fail but it succeeded", commandLine);
        } catch (Exception e) {
            if (!e.getMessage().contains(expectedMessage)) {
                HytaleAssert.fail("Command failed but message '%s' did not contain '%s'",
                        e.getMessage(), expectedMessage);
            }
        }
    }

    /**
     * Asserts that the sender received a message containing the expected substring.
     *
     * <p>Searches all messages sent to the sender for one containing the substring.</p>
     *
     * <p>Failure message: {@code "Expected sender to receive message containing 'text' but
     * messages were: [...]"}</p>
     *
     * @param sender            the mock command sender to inspect
     * @param expectedSubstring the substring to look for in received messages
     * @throws AssertionFailedException if no received message contains the substring
     */
    public static void assertSenderReceivedMessage(MockCommandSender sender, String expectedSubstring) {
        if (!sender.hasReceivedMessage(expectedSubstring)) {
            HytaleAssert.fail("Expected sender to receive message containing '%s' but messages were: %s",
                    expectedSubstring, sender.getMessages());
        }
    }

    /**
     * Asserts that the sender received exactly the expected number of messages.
     *
     * <p>Failure message: {@code "Expected sender to receive N messages but received M"}</p>
     *
     * @param sender the mock command sender to inspect
     * @param count  the exact number of messages expected
     * @throws AssertionFailedException if the message count does not match
     */
    public static void assertSenderReceivedMessageCount(MockCommandSender sender, int count) {
        int actual = sender.getMessages().size();
        if (actual != count) {
            HytaleAssert.fail("Expected sender to receive %d messages but received %d", count, actual);
        }
    }
}
