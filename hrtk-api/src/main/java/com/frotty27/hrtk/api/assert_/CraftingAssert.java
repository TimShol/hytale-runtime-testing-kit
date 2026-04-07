package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for crafting recipes - registry lookup and recipe counts.
 *
 * <p>Uses reflection to access recipe registries without importing HytaleServer.jar
 * classes directly. Searches common class paths for recipe manager and registry
 * types.</p>
 *
 * <pre>{@code
 * CraftingAssert.assertRecipeExists("Weapon_Sword_Wooden");
 * CraftingAssert.assertRecipeCount(50);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class CraftingAssert {

    private CraftingAssert() {}

    /**
     * Asserts that a recipe with the given ID exists in the recipe registry.
     *
     * <p>Uses reflection to locate the recipe registry and check whether a recipe
     * matching the given ID is present.</p>
     *
     * <p>Failure message: {@code "Expected recipe '<recipeId>' to exist in registry but it was not found"}</p>
     *
     * @param recipeId the recipe identifier to look up
     * @throws IllegalArgumentException    if recipeId is null
     * @throws AssertionFailedException    if the recipe is not found or the registry cannot be accessed
     */
    public static void assertRecipeExists(String recipeId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        Object registry = findRecipeRegistry();
        if (registry == null) {
            HytaleAssert.fail("Could not locate recipe registry via reflection");
        }
        boolean found = registryContainsRecipe(registry, recipeId);
        if (!found) {
            HytaleAssert.fail("Expected recipe '%s' to exist in registry but it was not found", recipeId);
        }
    }

    /**
     * Asserts that the total number of registered recipes is at least the given minimum.
     *
     * <p>Uses reflection to locate the recipe registry and retrieve its size.</p>
     *
     * <p>Failure message: {@code "Expected at least <minCount> recipes but found <actual>"}</p>
     *
     * @param minCount the minimum number of recipes expected
     * @throws AssertionFailedException if the recipe count is below the minimum or the registry cannot be accessed
     */
    public static void assertRecipeCount(int minCount) {
        Object registry = findRecipeRegistry();
        if (registry == null) {
            HytaleAssert.fail("Could not locate recipe registry via reflection");
        }
        int actual = getRegistrySize(registry);
        if (actual < minCount) {
            HytaleAssert.fail("Expected at least %d recipes but found %d", minCount, actual);
        }
    }

    private static Object findRecipeRegistry() {
        String[] candidates = {
                "com.hypixel.hytale.server.crafting.RecipeManager",
                "com.hypixel.hytale.server.crafting.RecipeRegistry",
                "com.hypixel.hytale.server.recipe.RecipeManager"
        };
        for (String className : candidates) {
            try {
                Class<?> clazz = Class.forName(className);
                for (Method method : clazz.getMethods()) {
                    if (("getInstance".equals(method.getName()) || "getRegistry".equals(method.getName()))
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

    private static boolean registryContainsRecipe(Object registry, String recipeId) {
        try {
            for (Method method : registry.getClass().getMethods()) {
                if (("get".equals(method.getName()) || "getRecipe".equals(method.getName())
                        || "contains".equals(method.getName()) || "has".equals(method.getName()))
                        && method.getParameterCount() == 1) {
                    Object result = method.invoke(registry, recipeId);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                    if (result != null) {
                        return true;
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to check recipe registry for '%s': %s", recipeId, e.getMessage());
        }
        return false;
    }

    private static int getRegistrySize(Object registry) {
        try {
            for (Method method : registry.getClass().getMethods()) {
                if (("size".equals(method.getName()) || "getRecipeCount".equals(method.getName())
                        || "count".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(registry);
                    if (result instanceof Number) {
                        return ((Number) result).intValue();
                    }
                }
            }
            for (Method method : registry.getClass().getMethods()) {
                if (("getAll".equals(method.getName()) || "getRecipes".equals(method.getName())
                        || "values".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(registry);
                    if (result instanceof java.util.Collection<?>) {
                        return ((java.util.Collection<?>) result).size();
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get recipe registry size: %s", e.getMessage());
        }
        HytaleAssert.fail("Could not determine recipe count from registry");
        return 0;
    }
}
