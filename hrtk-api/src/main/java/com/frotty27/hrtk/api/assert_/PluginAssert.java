package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for plugin management - checking loaded plugins and plugin counts.
 *
 * <p>Uses reflection to access the PluginManager without importing HytaleServer.jar
 * classes directly.</p>
 *
 * <pre>{@code
 * PluginAssert.assertPluginLoaded("my-plugin");
 * PluginAssert.assertPluginCount(3);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class PluginAssert {

    private PluginAssert() {}

    /**
     * Asserts that a plugin with the given name is loaded in the PluginManager.
     *
     * <p>Uses reflection to locate the PluginManager and check whether a plugin
     * matching the given name is present.</p>
     *
     * <p>Failure message: {@code "Expected plugin '<pluginName>' to be loaded but it was not found"}</p>
     *
     * @param pluginName the name of the plugin to check
     * @throws IllegalArgumentException    if pluginName is null
     * @throws AssertionFailedException    if the plugin is not loaded or the manager cannot be accessed
     */
    public static void assertPluginLoaded(String pluginName) {
        if (pluginName == null) {
            throw new IllegalArgumentException("pluginName must not be null");
        }
        Object manager = findPluginManager();
        if (manager == null) {
            HytaleAssert.fail("Could not locate PluginManager via reflection");
        }
        boolean found = isPluginLoaded(manager, pluginName);
        if (!found) {
            HytaleAssert.fail("Expected plugin '%s' to be loaded but it was not found", pluginName);
        }
    }

    /**
     * Asserts that the total number of loaded plugins equals the expected count.
     *
     * <p>Uses reflection to locate the PluginManager and retrieve its plugin count.</p>
     *
     * <p>Failure message: {@code "Expected <expected> loaded plugins but found <actual>"}</p>
     *
     * @param expected the expected number of loaded plugins
     * @throws AssertionFailedException if the plugin count does not match or the manager cannot be accessed
     */
    public static void assertPluginCount(int expected) {
        Object manager = findPluginManager();
        if (manager == null) {
            HytaleAssert.fail("Could not locate PluginManager via reflection");
        }
        int actual = getPluginCount(manager);
        if (actual != expected) {
            HytaleAssert.fail("Expected %d loaded plugins but found %d", expected, actual);
        }
    }

    private static Object findPluginManager() {
        String[] candidates = {
                "com.hypixel.hytale.server.plugin.PluginManager",
                "com.hypixel.hytale.server.plugins.PluginManager",
                "com.hypixel.hytale.server.mod.PluginManager"
        };
        for (String className : candidates) {
            try {
                Class<?> clazz = Class.forName(className);
                for (Method method : clazz.getMethods()) {
                    if (("getInstance".equals(method.getName()) || "get".equals(method.getName()))
                            && method.getParameterCount() == 0) {
                        Object instance = method.invoke(null);
                        if (instance != null) {
                            return instance;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static boolean isPluginLoaded(Object manager, String pluginName) {
        try {
            for (Method method : manager.getClass().getMethods()) {
                if (("getPlugin".equals(method.getName()) || "get".equals(method.getName()))
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == String.class) {
                    Object result = method.invoke(manager, pluginName);
                    if (result != null) {
                        return true;
                    }
                }
            }
            for (Method method : manager.getClass().getMethods()) {
                if (("isLoaded".equals(method.getName()) || "hasPlugin".equals(method.getName())
                        || "contains".equals(method.getName()))
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == String.class) {
                    Object result = method.invoke(manager, pluginName);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
            for (Method method : manager.getClass().getMethods()) {
                if (("getPlugins".equals(method.getName()) || "getAll".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(manager);
                    if (result instanceof Iterable<?>) {
                        for (Object plugin : (Iterable<?>) result) {
                            String name = getPluginName(plugin);
                            if (pluginName.equals(name)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to check if plugin '%s' is loaded: %s", pluginName, e.getMessage());
        }
        return false;
    }

    private static String getPluginName(Object plugin) {
        try {
            for (Method method : plugin.getClass().getMethods()) {
                if (("getName".equals(method.getName()) || "getId".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(plugin);
                    if (result != null) {
                        return result.toString();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int getPluginCount(Object manager) {
        try {
            for (Method method : manager.getClass().getMethods()) {
                if (("size".equals(method.getName()) || "getPluginCount".equals(method.getName())
                        || "count".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(manager);
                    if (result instanceof Number) {
                        return ((Number) result).intValue();
                    }
                }
            }
            for (Method method : manager.getClass().getMethods()) {
                if (("getPlugins".equals(method.getName()) || "getAll".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(manager);
                    if (result instanceof java.util.Collection<?>) {
                        return ((java.util.Collection<?>) result).size();
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get plugin count: %s", e.getMessage());
        }
        HytaleAssert.fail("Could not determine plugin count from PluginManager");
        return 0;
    }
}
