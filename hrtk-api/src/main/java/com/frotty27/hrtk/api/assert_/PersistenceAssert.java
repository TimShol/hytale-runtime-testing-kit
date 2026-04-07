package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for persistence - world save paths and player storage availability.
 *
 * <p>Uses reflection to access save-path and storage methods without importing
 * HytaleServer.jar classes directly.</p>
 *
 * <pre>{@code
 * PersistenceAssert.assertWorldSavePathExists(world);
 * PersistenceAssert.assertPlayerStorageAvailable();
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class PersistenceAssert {

    private PersistenceAssert() {}

    /**
     * Asserts that the world has a non-null save path configured.
     *
     * <p>Uses reflection to call save-path retrieval methods on the world object.</p>
     *
     * <p>Failure message: {@code "Expected world save path to be non-null but it was null"}</p>
     *
     * @param world the world object (runtime type: {@code World})
     * @throws IllegalArgumentException    if world is null
     * @throws AssertionFailedException    if the save path is null or cannot be determined
     */
    public static void assertWorldSavePathExists(Object world) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        Object savePath = getSavePath(world);
        if (savePath == null) {
            HytaleAssert.fail("Expected world save path to be non-null but it was null");
        }
    }

    /**
     * Asserts that the Universe player storage system is available.
     *
     * <p>Uses reflection to locate the Universe class and check that its player
     * storage accessor returns a non-null value.</p>
     *
     * <p>Failure message: {@code "Expected player storage to be available but it was not"}</p>
     *
     * @throws AssertionFailedException if player storage cannot be located or is null
     */
    public static void assertPlayerStorageAvailable() {
        Object storage = findPlayerStorage();
        if (storage == null) {
            HytaleAssert.fail("Expected player storage to be available but it was not");
        }
    }

    private static Object getSavePath(Object world) {
        try {
            for (Method method : world.getClass().getMethods()) {
                if (("getSavePath".equals(method.getName()) || "getWorldSavePath".equals(method.getName())
                        || "getSaveDirectory".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    return method.invoke(world);
                }
            }
            Object config = getWorldConfig(world);
            if (config != null) {
                for (Method method : config.getClass().getMethods()) {
                    if (("getSavePath".equals(method.getName()) || "getSaveDirectory".equals(method.getName()))
                            && method.getParameterCount() == 0) {
                        return method.invoke(config);
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get world save path via reflection: %s", e.getMessage());
        }
        HytaleAssert.fail("Could not determine world save path - no getSavePath() method found");
        return null;
    }

    private static Object getWorldConfig(Object world) {
        try {
            for (Method method : world.getClass().getMethods()) {
                if (("getConfig".equals(method.getName()) || "getWorldConfig".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    return method.invoke(world);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Object findPlayerStorage() {
        String[] candidates = {
                "com.hypixel.hytale.server.universe.Universe",
                "com.hypixel.hytale.server.Universe",
                "com.hypixel.hytale.server.persistence.Universe"
        };
        for (String className : candidates) {
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = null;
                for (Method method : clazz.getMethods()) {
                    if (("getInstance".equals(method.getName()) || "get".equals(method.getName()))
                            && method.getParameterCount() == 0) {
                        instance = method.invoke(null);
                        break;
                    }
                }
                if (instance != null) {
                    for (Method method : instance.getClass().getMethods()) {
                        if (("getPlayerStorage".equals(method.getName())
                                || "getPlayerDataStorage".equals(method.getName()))
                                && method.getParameterCount() == 0) {
                            return method.invoke(instance);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
