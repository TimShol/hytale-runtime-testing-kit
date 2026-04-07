package com.frotty27.hrtk.api.mock;

import java.util.List;
import java.util.Set;

/**
 * A fake command sender for testing commands without a real player connection.
 *
 * <p>Captures all messages sent to the sender and provides configurable
 * permissions. Created via {@link com.frotty27.hrtk.api.context.TestContext#createCommandSender()}
 * or {@link com.frotty27.hrtk.api.context.TestContext#createCommandSender(String...)}.</p>
 *
 * <pre>{@code
 * MockCommandSender sender = ctx.createCommandSender("admin.kick");
 * ctx.executeCommand("/kick player1", sender);
 * assertTrue(sender.hasReceivedMessage("has been kicked"));
 * assertEquals(1, sender.getMessages().size());
 * }</pre>
 *
 * @see com.frotty27.hrtk.api.context.TestContext
 * @see com.frotty27.hrtk.api.assert_.CommandAssert
 * @since 1.0.0
 */
public interface MockCommandSender {

    /**
     * Returns all messages sent to this sender, in the order they were received.
     *
     * @return an unmodifiable list of message strings
     */
    List<String> getMessages();

    /**
     * Returns the last message sent to this sender.
     *
     * @return the most recent message, or {@code null} if no messages have been received
     */
    String getLastMessage();

    /**
     * Checks whether any received message contains the given substring.
     *
     * @param substring the substring to search for
     * @return {@code true} if at least one message contains the substring
     */
    boolean hasReceivedMessage(String substring);

    /**
     * Clears all captured messages from this sender.
     */
    void clearMessages();

    /**
     * Returns the set of permissions currently granted to this sender.
     *
     * @return an unmodifiable set of permission strings
     */
    Set<String> getPermissions();

    /**
     * Checks whether this sender has a specific permission.
     *
     * @param permission the permission string to check
     * @return {@code true} if the sender has the permission
     */
    boolean hasPermission(String permission);

    /**
     * Grants a permission to this sender.
     *
     * @param permission the permission string to add
     */
    void addPermission(String permission);

    /**
     * Revokes a permission from this sender.
     *
     * @param permission the permission string to remove
     */
    void removePermission(String permission);

    /**
     * Returns the display name of this sender.
     *
     * @return the sender's display name
     */
    String getName();
}
