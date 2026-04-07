package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CraftingTestAdapter {

    private static final String BENCH_RECIPE_REGISTRY = "com.hypixel.hytale.builtin.crafting.BenchRecipeRegistry";
    private static final String CRAFTING_PLUGIN = "com.hypixel.hytale.builtin.crafting.CraftingPlugin";

    private CraftingTestAdapter() {}

    public static boolean recipeExists(String recipeId) {
        try {
            if (recipeId == null) return false;
            Class<?> registryClass = Class.forName(BENCH_RECIPE_REGISTRY);
            Method getMethod = registryClass.getMethod("get");
            Object registry = getMethod.invoke(null);
            if (registry == null) return false;
            for (Method m : registry.getClass().getMethods()) {
                if ("getRecipe".equals(m.getName()) && m.getParameterCount() == 1) {
                    Object result = m.invoke(registry, recipeId);
                    return result != null;
                }
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> listRecipeIds() {
        try {
            Class<?> registryClass = Class.forName(BENCH_RECIPE_REGISTRY);
            Method getMethod = registryClass.getMethod("get");
            Object registry = getMethod.invoke(null);
            if (registry == null) return Collections.emptyList();
            for (Method m : registry.getClass().getMethods()) {
                if ("getRecipeIds".equals(m.getName()) || "getKeys".equals(m.getName())) {
                    Object result = m.invoke(registry);
                    if (result instanceof Collection<?> col) {
                        List<String> ids = new ArrayList<>();
                        for (Object item : col) {
                            ids.add(String.valueOf(item));
                        }
                        return ids;
                    }
                }
            }
            return Collections.emptyList();
        } catch (Exception _) {
            return Collections.emptyList();
        }
    }

    public static boolean craftingPluginAvailable() {
        try {
            Class<?> pluginClass = Class.forName(CRAFTING_PLUGIN);
            return pluginClass != null;
        } catch (Exception _) {
            return false;
        }
    }
}
