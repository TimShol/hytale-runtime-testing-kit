package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PluginTestAdapter {

    private static final String PLUGIN_MANAGER = "com.hypixel.hytale.server.core.plugin.PluginManager";
    private static final String PLUGIN_BASE = "com.hypixel.hytale.server.core.plugin.PluginBase";

    private PluginTestAdapter() {}

    public static boolean isPluginLoaded(String name) {
        try {
            if (name == null) return false;
            Object manager = getPluginManager();
            if (manager == null) return false;
            Collection<?> plugins = getPluginsCollection(manager);
            if (plugins == null) return false;
            for (Object plugin : plugins) {
                String pluginName = getPluginName(plugin);
                if (name.equals(pluginName)) return true;
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }

    public static List<String> getPluginNames() {
        try {
            Object manager = getPluginManager();
            if (manager == null) return Collections.emptyList();
            Collection<?> plugins = getPluginsCollection(manager);
            if (plugins == null) return Collections.emptyList();
            List<String> names = new ArrayList<>();
            for (Object plugin : plugins) {
                String pluginName = getPluginName(plugin);
                if (pluginName != null) names.add(pluginName);
            }
            return names;
        } catch (Exception _) {
            return Collections.emptyList();
        }
    }

    public static Object getPlugin(String name) {
        try {
            if (name == null) return null;
            Object manager = getPluginManager();
            if (manager == null) return null;
            Collection<?> plugins = getPluginsCollection(manager);
            if (plugins == null) return null;
            for (Object plugin : plugins) {
                String pluginName = getPluginName(plugin);
                if (name.equals(pluginName)) return plugin;
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }

    private static Object getPluginManager() {
        try {
            Class<?> managerClass = Class.forName(PLUGIN_MANAGER);
            Method getMethod = managerClass.getMethod("get");
            return getMethod.invoke(null);
        } catch (Exception _) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<?> getPluginsCollection(Object manager) {
        try {
            for (Method m : manager.getClass().getMethods()) {
                if ("getPlugins".equals(m.getName()) && m.getParameterCount() == 0) {
                    Object result = m.invoke(manager);
                    if (result instanceof Collection<?> col) return col;
                }
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }

    private static String getPluginName(Object plugin) {
        try {
            if (plugin == null) return null;
            for (Method m : plugin.getClass().getMethods()) {
                if ("getName".equals(m.getName()) && m.getParameterCount() == 0) {
                    Object result = m.invoke(plugin);
                    return result != null ? result.toString() : null;
                }
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }
}
