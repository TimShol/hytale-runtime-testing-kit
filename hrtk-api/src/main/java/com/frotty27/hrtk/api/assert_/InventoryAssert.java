package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for inventory operations - slots, item stacks, and sections.
 *
 * <p>All methods accept {@code Object} for inventory and stack types to avoid
 * coupling the API module to HytaleServer.jar. At runtime, {@code inventory} is
 * an inventory container and {@code stack} is an {@code ItemStack}. Inventory
 * sections are accessed by index: storage (0), armor (1), and hotbar (2).</p>
 *
 * <pre>{@code
 * InventoryAssert.assertSlotContains(inv, SECTION_HOTBAR, 0, "Weapon_Sword_Iron", 1);
 * InventoryAssert.assertSlotEmpty(inv, SECTION_STORAGE, 5);
 * InventoryAssert.assertInventoryContains(inv, "Health_Potion");
 * InventoryAssert.assertInventoryEmpty(inv);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class InventoryAssert {

    /**
     * Section index for the main storage area.
     *
     * @see #assertSlotContains(Object, int, int, String, int)
     */
    public static final int SECTION_STORAGE = 0;

    /**
     * Section index for the armor slots.
     *
     * @see #assertSlotContains(Object, int, int, String, int)
     */
    public static final int SECTION_ARMOR   = 1;

    /**
     * Section index for the hotbar slots.
     *
     * @see #assertSlotContains(Object, int, int, String, int)
     */
    public static final int SECTION_HOTBAR  = 2;

    private InventoryAssert() {}

    /**
     * Asserts that a specific inventory slot contains the given item at the given quantity.
     *
     * <p>Failure message: {@code "Expected slot [section=S, slot=N] to contain <item> xQ but
     * it was empty"} or a mismatch variant for wrong item or quantity.</p>
     *
     * @param inventory the inventory object
     * @param section   the section index (0=storage, 1=armor, 2=hotbar)
     * @param slot      the slot index within the section
     * @param itemId    the expected item identifier
     * @param quantity  the expected quantity
     * @throws AssertionFailedException if the slot is empty, has the wrong item, or wrong quantity
     */
    public static void assertSlotContains(Object inventory, int section, int slot,
                                          String itemId, int quantity) {
        Object sectionObj = getSection(inventory, section);
        Object stack = getSlot(sectionObj, slot);
        if (stack == null || isEmptyStack(stack)) {
            HytaleAssert.fail("Expected slot [section=%d, slot=%d] to contain <%s> x%d but it was empty",
                    section, slot, itemId, quantity);
        }
        String actualId = getItemId(stack);
        int actualQty = getQuantity(stack);
        if (!itemId.equals(actualId)) {
            HytaleAssert.fail("Expected slot [section=%d, slot=%d] to contain item <%s> but was <%s>",
                    section, slot, itemId, actualId);
        }
        if (actualQty != quantity) {
            HytaleAssert.fail("Expected slot [section=%d, slot=%d] to have quantity %d but was %d",
                    section, slot, quantity, actualQty);
        }
    }

    /**
     * Asserts that a specific inventory slot is empty.
     *
     * <p>Failure message: {@code "Expected slot [section=S, slot=N] to be empty but contained
     * <item> xQ"}</p>
     *
     * @param inventory the inventory object
     * @param section   the section index (0=storage, 1=armor, 2=hotbar)
     * @param slot      the slot index within the section
     * @throws AssertionFailedException if the slot is not empty
     */
    public static void assertSlotEmpty(Object inventory, int section, int slot) {
        Object sectionObj = getSection(inventory, section);
        Object stack = getSlot(sectionObj, slot);
        if (stack != null && !isEmptyStack(stack)) {
            HytaleAssert.fail("Expected slot [section=%d, slot=%d] to be empty but contained <%s> x%d",
                    section, slot, getItemId(stack), getQuantity(stack));
        }
    }

    /**
     * Asserts that the inventory contains at least one stack with the given item ID
     * across all sections (storage, armor, hotbar).
     *
     * <p>Failure message: {@code "Expected inventory to contain item <item> but it was not found"}</p>
     *
     * @param inventory the inventory object
     * @param itemId    the item identifier to search for
     * @throws AssertionFailedException if the item is not found in any section
     */
    public static void assertInventoryContains(Object inventory, String itemId) {
        for (int section = 0; section <= 2; section++) {
            Object sectionObj = getSectionSafe(inventory, section);
            if (sectionObj == null) continue;
            int size = getSectionSize(sectionObj);
            for (int slot = 0; slot < size; slot++) {
                Object stack = getSlot(sectionObj, slot);
                if (stack != null && !isEmptyStack(stack)) {
                    if (itemId.equals(getItemId(stack))) {
                        return;
                    }
                }
            }
        }
        HytaleAssert.fail("Expected inventory to contain item <%s> but it was not found", itemId);
    }

    /**
     * Asserts that the entire inventory is empty across all sections and slots.
     *
     * <p>Failure message: {@code "Expected inventory to be empty but slot [section=S, slot=N]
     * contains <item> xQ"}</p>
     *
     * @param inventory the inventory object
     * @throws AssertionFailedException if any slot contains an item
     */
    public static void assertInventoryEmpty(Object inventory) {
        for (int section = 0; section <= 2; section++) {
            Object sectionObj = getSectionSafe(inventory, section);
            if (sectionObj == null) continue;
            int size = getSectionSize(sectionObj);
            for (int slot = 0; slot < size; slot++) {
                Object stack = getSlot(sectionObj, slot);
                if (stack != null && !isEmptyStack(stack)) {
                    HytaleAssert.fail("Expected inventory to be empty but slot [section=%d, slot=%d] contains <%s> x%d",
                            section, slot, getItemId(stack), getQuantity(stack));
                }
            }
        }
    }

    /**
     * Asserts that an item stack has the expected item ID and quantity.
     *
     * <p>Failure message: {@code "Expected item <expected> but was <actual>"} or
     * {@code "Expected quantity N but was M"}</p>
     *
     * @param stack          the item stack object
     * @param expectedItemId the expected item identifier
     * @param expectedQty    the expected quantity
     * @throws AssertionFailedException if the item ID or quantity does not match
     */
    public static void assertItemStackEquals(Object stack, String expectedItemId, int expectedQty) {
        HytaleAssert.assertNotNull("ItemStack", stack);
        String actualId = getItemId(stack);
        int actualQty = getQuantity(stack);
        if (!expectedItemId.equals(actualId)) {
            HytaleAssert.fail("Expected item <%s> but was <%s>", expectedItemId, actualId);
        }
        if (actualQty != expectedQty) {
            HytaleAssert.fail("Expected quantity %d but was %d", expectedQty, actualQty);
        }
    }

    private static Object getSection(Object inventory, int section) {
        String methodName = sectionMethodName(section);
        try {
            for (Method method : inventory.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    return method.invoke(inventory);
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get inventory section %d via %s(): %s",
                    section, methodName, e.getMessage());
        }
        HytaleAssert.fail("Inventory does not have method %s()", methodName);
        return null;
    }

    private static Object getSectionSafe(Object inventory, int section) {
        String methodName = sectionMethodName(section);
        try {
            for (Method method : inventory.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    return method.invoke(inventory);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String sectionMethodName(int section) {
        switch (section) {
            case SECTION_STORAGE: return "getStorage";
            case SECTION_ARMOR:   return "getArmor";
            case SECTION_HOTBAR:  return "getHotbar";
            default:
                HytaleAssert.fail("Unknown inventory section: %d", section);
                return null;
        }
    }

    private static Object getSlot(Object sectionObj, int slot) {
        try {
            for (Method method : sectionObj.getClass().getMethods()) {
                if ("getSlot".equals(method.getName()) && method.getParameterCount() == 1) {
                    return method.invoke(sectionObj, slot);
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get slot %d: %s", slot, e.getMessage());
        }
        HytaleAssert.fail("Section does not have getSlot(int) method");
        return null;
    }

    private static int getSectionSize(Object sectionObj) {
        try {
            for (Method method : sectionObj.getClass().getMethods()) {
                if (("getSize".equals(method.getName()) || "size".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(sectionObj);
                    if (result instanceof Number) {
                        return ((Number) result).intValue();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
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
            HytaleAssert.fail("Failed to get item ID from stack: %s", e.getMessage());
        }
        HytaleAssert.fail("ItemStack does not have getItemId() method");
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
            HytaleAssert.fail("Failed to get quantity from stack: %s", e.getMessage());
        }
        HytaleAssert.fail("ItemStack does not have getQuantity() method");
        return 0;
    }

    private static boolean isEmptyStack(Object stack) {
        try {
            for (Method method : stack.getClass().getMethods()) {
                if ("isEmpty".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(stack);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
            int qty = getQuantity(stack);
            return qty <= 0;
        } catch (Exception ignored) {
            return false;
        }
    }
}
