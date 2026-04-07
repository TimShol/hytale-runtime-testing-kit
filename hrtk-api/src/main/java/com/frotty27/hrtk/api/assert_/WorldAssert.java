package com.frotty27.hrtk.api.assert_;

/**
 * Assertions for world state - blocks, chunks, and entity presence.
 *
 * <p>Uses {@code Object} types to avoid coupling to HytaleServer.jar.
 * At runtime, {@code world} is a {@code World} and {@code ref} is a
 * {@code Ref<EntityStore>}.</p>
 *
 * <pre>{@code
 * WorldAssert.assertBlockAt(world, 10, 64, 10, "Rock_Stone");
 * WorldAssert.assertBlockNotAt(world, 10, 64, 10, "Empty");
 * WorldAssert.assertWorldExists("test_world");
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class WorldAssert {

    private WorldAssert() {}

    /**
     * Asserts that the block at the given coordinates is the expected type.
     *
     * <p>Failure message: {@code "Expected block <expected> at (x, y, z) but was <actual>"}</p>
     *
     * @param world               the world object (runtime type: {@code World})
     * @param x                   the block X coordinate
     * @param y                   the block Y coordinate
     * @param z                   the block Z coordinate
     * @param expectedBlockTypeId the expected block type identifier (e.g., {@code "Rock_Stone"})
     * @throws AssertionFailedException if the block at the coordinates is not the expected type
     */
    public static void assertBlockAt(Object world, int x, int y, int z, String expectedBlockTypeId) {
        String actual = getBlockReflective(world, x, y, z);
        if (actual == null) {
            HytaleAssert.fail("Could not read block at (%d, %d, %d) - block query returned null",
                    x, y, z);
        }
        if (!expectedBlockTypeId.equals(actual)) {
            HytaleAssert.fail("Expected block <%s> at (%d, %d, %d) but was <%s>",
                    expectedBlockTypeId, x, y, z, actual);
        }
    }

    /**
     * Asserts that the block at the given coordinates is NOT the specified type.
     *
     * <p>Failure message: {@code "Expected block at (x, y, z) to NOT be <type>"}</p>
     *
     * @param world                 the world object (runtime type: {@code World})
     * @param x                     the block X coordinate
     * @param y                     the block Y coordinate
     * @param z                     the block Z coordinate
     * @param unexpectedBlockTypeId the block type identifier that should not be present
     * @throws AssertionFailedException if the block at the coordinates matches the unexpected type
     */
    public static void assertBlockNotAt(Object world, int x, int y, int z, String unexpectedBlockTypeId) {
        String actual = getBlockReflective(world, x, y, z);
        if (actual == null) {
            HytaleAssert.fail("Could not read block at (%d, %d, %d) - block query returned null",
                    x, y, z);
        }
        if (unexpectedBlockTypeId.equals(actual)) {
            HytaleAssert.fail("Expected block at (%d, %d, %d) to NOT be <%s>",
                    x, y, z, unexpectedBlockTypeId);
        }
    }

    /**
     * Asserts that an entity exists in the given world by verifying both the world
     * and entity reference are non-null.
     *
     * <p>Failure message: {@code "World: Expected non-null value but was null"} or
     * {@code "Entity reference: Expected non-null value but was null"}</p>
     *
     * @param world the world object (runtime type: {@code World})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if either the world or entity reference is {@code null}
     */
    public static void assertEntityInWorld(Object world, Object ref) {
        HytaleAssert.assertNotNull("World", world);
        HytaleAssert.assertNotNull("Entity reference", ref);
    }

    /**
     * Asserts that a world with the given name exists in the universe.
     *
     * <p>Uses reflection to access {@code Universe.get().getWorlds()} and checks
     * whether the returned map contains the given world name as a key.</p>
     *
     * <p>Failure message: {@code "Expected world 'name' to exist but it does not.
     * Available worlds: [...]"}</p>
     *
     * @param worldName the name of the world to check for
     * @throws AssertionFailedException if the world does not exist or reflection fails
     */
    public static void assertWorldExists(String worldName) {
        try {
            var universeClass = Class.forName("com.hypixel.hytale.server.core.universe.Universe");
            var getInstance = universeClass.getMethod("get");
            var universe = getInstance.invoke(null);
            var getWorlds = universe.getClass().getMethod("getWorlds");
            @SuppressWarnings("unchecked")
            var worlds = (java.util.Map<String, ?>) getWorlds.invoke(universe);
            if (!worlds.containsKey(worldName)) {
                HytaleAssert.fail("Expected world '%s' to exist but it does not. Available worlds: %s",
                        worldName, worlds.keySet());
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to check world existence: %s", e.getMessage());
        }
    }

    /**
     * Asserts that the given world is currently ticking.
     *
     * <p>Checks the {@code isTicking()} method on the world object via reflection.</p>
     *
     * <p>Failure message: {@code "Expected world to be ticking but it was not"}</p>
     *
     * @param world the world object (runtime type: {@code World})
     * @throws AssertionFailedException if the world is null or not ticking
     */
    public static void assertWorldTicking(Object world) {
        if (world == null) {
            HytaleAssert.fail("world must not be null");
        }
        boolean ticking = invokeBooleanMethod(world, "isTicking");
        if (!ticking) {
            HytaleAssert.fail("Expected world to be ticking but it was not");
        }
    }

    /**
     * Asserts that the given world is currently paused.
     *
     * <p>Checks the {@code isPaused()} method on the world object via reflection.</p>
     *
     * <p>Failure message: {@code "Expected world to be paused but it was not"}</p>
     *
     * @param world the world object (runtime type: {@code World})
     * @throws AssertionFailedException if the world is null or not paused
     */
    public static void assertWorldPaused(Object world) {
        if (world == null) {
            HytaleAssert.fail("world must not be null");
        }
        boolean paused = invokeBooleanMethod(world, "isPaused");
        if (!paused) {
            HytaleAssert.fail("Expected world to be paused but it was not");
        }
    }

    private static boolean invokeBooleanMethod(Object target, String methodName) {
        try {
            for (var method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return false;
    }

    private static String getBlockReflective(Object world, int x, int y, int z) {
        try {
            var getBlockMethod = world.getClass().getMethod("getBlock", int.class, int.class, int.class);
            Object block = getBlockMethod.invoke(world, x, y, z);
            if (block != null) {
                return block.toString();
            }
        } catch (Exception _) {}
        try {
            var getBlockTypeMethod = world.getClass().getMethod("getBlockType", int.class, int.class, int.class);
            Object blockType = getBlockTypeMethod.invoke(world, x, y, z);
            if (blockType != null) {
                for (var method : blockType.getClass().getMethods()) {
                    if ("getId".equals(method.getName()) && method.getParameterCount() == 0) {
                        Object id = method.invoke(blockType);
                        if (id != null) return id.toString();
                    }
                }
                return blockType.toString();
            }
        } catch (Exception _) {}
        return null;
    }
}
