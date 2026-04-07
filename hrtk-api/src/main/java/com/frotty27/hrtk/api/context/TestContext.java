package com.frotty27.hrtk.api.context;

import com.frotty27.hrtk.api.mock.EventCapture;
import com.frotty27.hrtk.api.mock.MockCommandSender;
import com.frotty27.hrtk.api.mock.MockPlayerRef;

/**
 * Base test context provided to all test methods that request it.
 *
 * <p>Gives access to the plugin being tested, logging, event capture, command
 * sender creation, and command execution. This is the root context interface;
 * specialized contexts like {@link EcsTestContext}, {@link WorldTestContext},
 * and {@link BenchmarkContext} extend it with additional capabilities.</p>
 *
 * <p>Available for injection in any test method. Annotate your test class with
 * any HRTK test annotation (e.g., {@code @HytaleTest}, {@code @EcsTest},
 * {@code @WorldTest}) and declare a {@code TestContext} parameter.</p>
 *
 * <pre>{@code
 * @HytaleTest
 * public void myTest(TestContext ctx) {
 *     ctx.log("Running test for %s", ctx.getPluginName());
 *     EventCapture<MyEvent> capture = ctx.captureEvent(MyEvent.class);
 *     MockCommandSender sender = ctx.createCommandSender("my.permission");
 *     ctx.executeCommand("/mycommand arg1", sender);
 * }
 * }</pre>
 *
 * @see EcsTestContext
 * @see WorldTestContext
 * @see BenchmarkContext
 * @since 1.0.0
 */
public interface TestContext {

    /**
     * Returns the name of the plugin whose tests are currently running.
     *
     * @return the plugin name, never {@code null}
     */
    String getPluginName();

    /**
     * Logs a message to the test output.
     *
     * @param message the message to log
     */
    void log(String message);

    /**
     * Logs a formatted message to the test output.
     *
     * @param format the format string (as per {@link String#format(String, Object...)})
     * @param args   the format arguments
     */
    void log(String format, Object... args);

    /**
     * Creates an event capture that records all events of the given type fired
     * during the remainder of the test.
     *
     * <p>The returned capture starts listening immediately. Call
     * {@link EventCapture#close()} to stop capturing early, or let the framework
     * clean it up at the end of the test.</p>
     *
     * @param eventType the event class to capture
     * @param <E>       the event type
     * @return an {@link EventCapture} that records all matching events
     */
    <E> EventCapture<E> captureEvent(Class<E> eventType);

    /**
     * Creates a mock command sender with no permissions.
     *
     * @return a new {@link MockCommandSender} with an empty permission set
     */
    MockCommandSender createCommandSender();

    /**
     * Creates a mock command sender pre-configured with the given permissions.
     *
     * @param permissions the permission strings to grant to the sender
     * @return a new {@link MockCommandSender} with the specified permissions
     */
    MockCommandSender createCommandSender(String... permissions);

    /**
     * Executes a command string as the given sender.
     *
     * <p>The command is dispatched through the server's command system as if the
     * sender had typed it. Any messages sent back to the sender are captured
     * by the {@link MockCommandSender}.</p>
     *
     * @param commandLine the full command string to execute (e.g., {@code "/ban player1"})
     * @param sender      the mock command sender to execute as
     */
    void executeCommand(String commandLine, MockCommandSender sender);

    /**
     * Creates a mock player reference for testing player-related logic.
     *
     * @return a new {@link MockPlayerRef} with a random UUID and default display name
     */
    MockPlayerRef createMockPlayer();

    /**
     * Creates a mock player reference with the given display name.
     *
     * @param displayName the display name for the mock player
     * @return a new {@link MockPlayerRef} with a random UUID and the specified name
     */
    MockPlayerRef createMockPlayer(String displayName);
}
