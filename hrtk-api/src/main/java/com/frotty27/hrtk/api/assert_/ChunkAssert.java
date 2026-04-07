package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for chunk loading state - verifying chunks are loaded or unloaded.
 *
 * <p>Uses reflection to access chunk management methods on the world object
 * without importing HytaleServer.jar classes directly.</p>
 *
 * <pre>{@code
 * ChunkAssert.assertChunkLoaded(world, 0, 0);
 * ChunkAssert.assertChunkNotLoaded(world, 100, 100);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class ChunkAssert {

    private ChunkAssert() {}

    /**
     * Asserts that the chunk at the given coordinates is loaded in the world.
     *
     * <p>Uses reflection to call chunk-checking methods such as {@code isChunkLoaded()}
     * or {@code getChunk()} on the world object.</p>
     *
     * <p>Failure message: {@code "Expected chunk at (<chunkX>, <chunkZ>) to be loaded but it was not"}</p>
     *
     * @param world  the world object (runtime type: {@code World})
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @throws IllegalArgumentException    if world is null
     * @throws AssertionFailedException    if the chunk is not loaded
     */
    public static void assertChunkLoaded(Object world, int chunkX, int chunkZ) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        boolean loaded = isChunkLoaded(world, chunkX, chunkZ);
        if (!loaded) {
            HytaleAssert.fail("Expected chunk at (%d, %d) to be loaded but it was not", chunkX, chunkZ);
        }
    }

    /**
     * Asserts that the chunk at the given coordinates is not loaded in the world.
     *
     * <p>Uses reflection to call chunk-checking methods such as {@code isChunkLoaded()}
     * or {@code getChunk()} on the world object.</p>
     *
     * <p>Failure message: {@code "Expected chunk at (<chunkX>, <chunkZ>) to not be loaded but it was"}</p>
     *
     * @param world  the world object (runtime type: {@code World})
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @throws IllegalArgumentException    if world is null
     * @throws AssertionFailedException    if the chunk is loaded
     */
    public static void assertChunkNotLoaded(Object world, int chunkX, int chunkZ) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        boolean loaded = isChunkLoaded(world, chunkX, chunkZ);
        if (loaded) {
            HytaleAssert.fail("Expected chunk at (%d, %d) to not be loaded but it was", chunkX, chunkZ);
        }
    }

    private static boolean isChunkLoaded(Object world, int chunkX, int chunkZ) {
        try {
            for (Method method : world.getClass().getMethods()) {
                if ("isChunkLoaded".equals(method.getName()) && method.getParameterCount() == 2) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params[0] == int.class && params[1] == int.class) {
                        Object result = method.invoke(world, chunkX, chunkZ);
                        if (result instanceof Boolean) {
                            return (Boolean) result;
                        }
                    }
                }
            }
            for (Method method : world.getClass().getMethods()) {
                if ("getChunk".equals(method.getName()) && method.getParameterCount() == 2) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params[0] == int.class && params[1] == int.class) {
                        Object result = method.invoke(world, chunkX, chunkZ);
                        return result != null;
                    }
                }
            }
            Object chunkManager = getChunkManager(world);
            if (chunkManager != null) {
                for (Method method : chunkManager.getClass().getMethods()) {
                    if ("isLoaded".equals(method.getName()) && method.getParameterCount() == 2) {
                        Class<?>[] params = method.getParameterTypes();
                        if (params[0] == int.class && params[1] == int.class) {
                            Object result = method.invoke(chunkManager, chunkX, chunkZ);
                            if (result instanceof Boolean) {
                                return (Boolean) result;
                            }
                        }
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to check chunk loaded state at (%d, %d): %s", chunkX, chunkZ, e.getMessage());
        }
        HytaleAssert.fail("Could not determine chunk loaded state - no isChunkLoaded() or getChunk() method found");
        return false;
    }

    private static Object getChunkManager(Object world) {
        try {
            for (Method method : world.getClass().getMethods()) {
                if (("getChunkManager".equals(method.getName()) || "getChunkProvider".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    return method.invoke(world);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
