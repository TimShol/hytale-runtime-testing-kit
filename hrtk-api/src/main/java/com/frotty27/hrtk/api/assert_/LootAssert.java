package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Assertions for loot drops and item drop lists.
 *
 * <p>Drop lists are {@code List<?>} at the API level to avoid coupling to
 * HytaleServer.jar types. At runtime, elements are {@code ItemStack} objects
 * whose item ID and quantity are accessed reflectively.</p>
 *
 * <pre>{@code
 * List<?> drops = simulateDrop(lootTable);
 * LootAssert.assertDropsContain(drops, "Ingot_Gold");
 * LootAssert.assertDropsContain(drops, "Ingot_Gold", 3);
 * LootAssert.assertDropCountBetween(drops, 1, 5);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class LootAssert {

    private LootAssert() {}

    /**
     * Asserts that the drops contain at least one stack with the given item ID.
     *
     * <p>Failure message: {@code "Expected drops to contain item <item> but it was not found
     * (drops: N items)"}</p>
     *
     * @param drops  the list of dropped item stacks
     * @param itemId the expected item identifier
     * @throws AssertionFailedException if no stack in the list has the given item ID
     */
    public static void assertDropsContain(List<?> drops, String itemId) {
        HytaleAssert.assertNotNull("Drops list", drops);
        for (Object stack : drops) {
            if (itemId.equals(getItemId(stack))) {
                return;
            }
        }
        HytaleAssert.fail("Expected drops to contain item <%s> but it was not found (drops: %d items)",
                itemId, drops.size());
    }

    /**
     * Asserts that the drops contain the given item ID with at least the specified
     * total quantity summed across all matching stacks.
     *
     * <p>Failure message: {@code "Expected drops to contain at least N of <item> but found M"}</p>
     *
     * @param drops       the list of dropped item stacks
     * @param itemId      the expected item identifier
     * @param minQuantity the minimum total quantity across all matching stacks
     * @throws AssertionFailedException if the total quantity of the item is below the minimum
     */
    public static void assertDropsContain(List<?> drops, String itemId, int minQuantity) {
        HytaleAssert.assertNotNull("Drops list", drops);
        int total = 0;
        for (Object stack : drops) {
            if (itemId.equals(getItemId(stack))) {
                total += getQuantity(stack);
            }
        }
        if (total < minQuantity) {
            HytaleAssert.fail("Expected drops to contain at least %d of <%s> but found %d",
                    minQuantity, itemId, total);
        }
    }

    /**
     * Asserts that the drops list has exactly the expected number of stacks.
     *
     * <p>Failure message: {@code "Expected N drop stacks but got M"}</p>
     *
     * @param drops    the list of dropped item stacks
     * @param expected the exact number of stacks expected
     * @throws AssertionFailedException if the list size does not match
     */
    public static void assertDropCount(List<?> drops, int expected) {
        HytaleAssert.assertNotNull("Drops list", drops);
        if (drops.size() != expected) {
            HytaleAssert.fail("Expected %d drop stacks but got %d", expected, drops.size());
        }
    }

    /**
     * Asserts that the drops list size is between {@code min} and {@code max} (inclusive).
     *
     * <p>Failure message: {@code "Expected between N and M drop stacks but got K"}</p>
     *
     * @param drops the list of dropped item stacks
     * @param min   the minimum number of stacks (inclusive)
     * @param max   the maximum number of stacks (inclusive)
     * @throws AssertionFailedException if the list size is outside the range
     */
    public static void assertDropCountBetween(List<?> drops, int min, int max) {
        HytaleAssert.assertNotNull("Drops list", drops);
        if (drops.size() < min || drops.size() > max) {
            HytaleAssert.fail("Expected between %d and %d drop stacks but got %d",
                    min, max, drops.size());
        }
    }

    /**
     * Asserts that the drops list is empty (no drops at all).
     *
     * <p>Failure message: {@code "Expected no drops but got N stacks"}</p>
     *
     * @param drops the list of dropped item stacks
     * @throws AssertionFailedException if the list is not empty
     */
    public static void assertNoDrops(List<?> drops) {
        HytaleAssert.assertNotNull("Drops list", drops);
        if (!drops.isEmpty()) {
            HytaleAssert.fail("Expected no drops but got %d stacks", drops.size());
        }
    }

    private static String getItemId(Object stack) {
        try {
            for (Method method : stack.getClass().getMethods()) {
                if ("getItemId".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(stack);
                    return result != null ? result.toString() : null;
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get item ID from drop stack: %s", e.getMessage());
        }
        HytaleAssert.fail("Drop stack does not have getItemId() method");
        return null;
    }

    private static int getQuantity(Object stack) {
        try {
            for (Method method : stack.getClass().getMethods()) {
                if ("getQuantity".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(stack);
                    if (result instanceof Number) {
                        return ((Number) result).intValue();
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get quantity from drop stack: %s", e.getMessage());
        }
        HytaleAssert.fail("Drop stack does not have getQuantity() method");
        return 0;
    }
}
