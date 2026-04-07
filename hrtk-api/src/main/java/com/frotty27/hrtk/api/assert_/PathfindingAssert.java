package com.frotty27.hrtk.api.assert_;

/**
 * Assertions for pathfinding system availability.
 *
 * <p>Uses reflection to check whether AStar pathfinding classes are present
 * on the classpath without importing HytaleServer.jar directly.</p>
 *
 * <pre>{@code
 * PathfindingAssert.assertPathfindingAvailable();
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class PathfindingAssert {

    private PathfindingAssert() {}

    /**
     * Asserts that AStar pathfinding classes are available on the classpath.
     *
     * <p>Searches common package paths for AStar-related classes using
     * {@link Class#forName(String)}.</p>
     *
     * <p>Failure message: {@code "Expected AStar pathfinding classes to be available on the classpath but none were found"}</p>
     *
     * @throws AssertionFailedException if no AStar pathfinding class can be located
     */
    public static void assertPathfindingAvailable() {
        String[] candidates = {
                "com.hypixel.hytale.server.pathfinding.AStar",
                "com.hypixel.hytale.server.ai.pathfinding.AStar",
                "com.hypixel.hytale.server.navigation.AStar",
                "com.hypixel.hytale.server.pathfinding.AStarPathfinder",
                "com.hypixel.hytale.server.ai.pathfinding.AStarPathfinder"
        };
        for (String className : candidates) {
            try {
                Class.forName(className);
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }
        HytaleAssert.fail("Expected AStar pathfinding classes to be available on the classpath but none were found");
    }
}
