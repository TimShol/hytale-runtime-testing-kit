package com.frotty27.hrtk.api.mock;

import java.util.UUID;

/**
 * A fake player reference for testing player-related logic without a real connection.
 *
 * <p>Provides a stable UUID and display name for use in tests that require
 * player identity without establishing an actual network connection.</p>
 *
 * <pre>{@code
 * MockPlayerRef player = ctx.createMockPlayer();
 * assertEquals("TestPlayer", player.getDisplayName());
 * assertNotNull(player.getUuid());
 * }</pre>
 *
 * @see MockCommandSender
 * @since 1.0.0
 */
public interface MockPlayerRef {

    /**
     * Returns the mock player's UUID.
     *
     * <p>This UUID is randomly generated at creation time and remains stable
     * for the lifetime of the mock player.</p>
     *
     * @return the player's UUID, never {@code null}
     */
    UUID getUuid();

    /**
     * Returns the mock player's display name.
     *
     * @return the player's display name, never {@code null}
     */
    String getDisplayName();
}
