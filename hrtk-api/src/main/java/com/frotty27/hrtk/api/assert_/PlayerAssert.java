package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for player state - game mode, name, world, and alive status.
 *
 * <p>All methods accept {@code Object} for player, store, and ref types to avoid
 * coupling the API module to HytaleServer.jar. At runtime, {@code player} maps to
 * a player instance, and store/ref map to {@code Store<EntityStore>} and
 * {@code Ref<EntityStore>}. Player properties are accessed reflectively.</p>
 *
 * <pre>{@code
 * PlayerAssert.assertGameMode(player, "CREATIVE");
 * PlayerAssert.assertPlayerName(player, "Frotty27");
 * PlayerAssert.assertPlayerInWorld(player, "overworld");
 * PlayerAssert.assertPlayerAlive(store, ref);
 * }</pre>
 *
 * @see StatsAssert
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class PlayerAssert {

    private PlayerAssert() {}

    /**
     * Asserts that a player's game mode matches the expected value.
     *
     * <p>Retrieves the game mode via reflection on the player object and compares
     * the string representation to the expected value.</p>
     *
     * <p>Failure message: {@code "Expected game mode <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * PlayerAssert.assertGameMode(player, "CREATIVE");
     * }</pre>
     *
     * @param player           the player object
     * @param expectedGameMode the expected game mode name
     * @throws IllegalArgumentException if player or expectedGameMode is null
     * @throws AssertionFailedException if the game mode does not match
     */
    public static void assertGameMode(Object player, String expectedGameMode) {
        if (player == null) {
            throw new IllegalArgumentException("player must not be null");
        }
        if (expectedGameMode == null) {
            throw new IllegalArgumentException("expectedGameMode must not be null");
        }
        String actual = invokeStringMethod(player, "getGameMode");
        if (!expectedGameMode.equals(actual)) {
            HytaleAssert.fail("Expected game mode <%s> but was <%s>", expectedGameMode, actual);
        }
    }

    /**
     * Asserts that a player's name matches the expected value.
     *
     * <p>Retrieves the name via reflection on the player object.</p>
     *
     * <p>Failure message: {@code "Expected player name <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * PlayerAssert.assertPlayerName(player, "Frotty27");
     * }</pre>
     *
     * @param player       the player object
     * @param expectedName the expected player name
     * @throws IllegalArgumentException if player or expectedName is null
     * @throws AssertionFailedException if the name does not match
     */
    public static void assertPlayerName(Object player, String expectedName) {
        if (player == null) {
            throw new IllegalArgumentException("player must not be null");
        }
        if (expectedName == null) {
            throw new IllegalArgumentException("expectedName must not be null");
        }
        String actual = invokeStringMethod(player, "getName");
        if (!expectedName.equals(actual)) {
            HytaleAssert.fail("Expected player name <%s> but was <%s>", expectedName, actual);
        }
    }

    /**
     * Asserts that a player is in the world with the expected name.
     *
     * <p>Retrieves the player's world via {@code getWorld()} and then calls
     * {@code getName()} on the world object to compare.</p>
     *
     * <p>Failure message: {@code "Expected player to be in world <expected> but was in <actual>"} or
     * {@code "Expected player to be in world <expected> but getWorld() returned null"}</p>
     *
     * <pre>{@code
     * PlayerAssert.assertPlayerInWorld(player, "overworld");
     * }</pre>
     *
     * @param player            the player object
     * @param expectedWorldName the expected world name
     * @throws IllegalArgumentException if player or expectedWorldName is null
     * @throws AssertionFailedException if the player is not in the expected world
     */
    public static void assertPlayerInWorld(Object player, String expectedWorldName) {
        if (player == null) {
            throw new IllegalArgumentException("player must not be null");
        }
        if (expectedWorldName == null) {
            throw new IllegalArgumentException("expectedWorldName must not be null");
        }
        Object world = invokeObjectMethod(player, "getWorld");
        if (world == null) {
            HytaleAssert.fail("Expected player to be in world <%s> but getWorld() returned null",
                    expectedWorldName);
        }
        String actualWorldName = invokeStringMethod(world, "getName");
        if (!expectedWorldName.equals(actualWorldName)) {
            HytaleAssert.fail("Expected player to be in world <%s> but was in <%s>",
                    expectedWorldName, actualWorldName);
        }
    }

    /**
     * Asserts that the player's entity is alive.
     *
     * <p>Delegates to {@link StatsAssert#assertAlive(Object, Object)} using the
     * provided store and entity reference.</p>
     *
     * <p>Failure message: {@code "Expected entity to be alive but health was <health>"} or
     * {@code "Expected entity to be alive but it has a DeathComponent"}</p>
     *
     * <pre>{@code
     * PlayerAssert.assertPlayerAlive(store, ref);
     * }</pre>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if the player entity is dead
     * @see StatsAssert#assertAlive(Object, Object)
     */
    public static void assertPlayerAlive(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        StatsAssert.assertAlive(store, ref);
    }

    private static String invokeStringMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    return result != null ? result.toString() : null;
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return null;
    }

    private static Object invokeObjectMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    return method.invoke(target);
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return null;
    }
}
