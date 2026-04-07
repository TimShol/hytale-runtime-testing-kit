package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for item stacks - identity, quantity, durability, emptiness, and metadata.
 *
 * <p>All methods accept {@code Object} for item stack types to avoid coupling the
 * API module to HytaleServer.jar. At runtime, {@code itemStack} maps to an
 * {@code ItemStack} instance. Properties are accessed reflectively.</p>
 *
 * <pre>{@code
 * ItemAssert.assertItemId(stack, "Weapon_Sword_Iron");
 * ItemAssert.assertItemQuantity(stack, 5);
 * ItemAssert.assertItemNotEmpty(stack);
 * ItemAssert.assertItemDurability(stack, 80.0, 1.0);
 * ItemAssert.assertItemStackable(stackA, stackB);
 * ItemAssert.assertItemMetadata(stack, "enchantment", "fire_aspect");
 * }</pre>
 *
 * @see InventoryAssert
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class ItemAssert {

    private ItemAssert() {}

    /**
     * Asserts that an item stack's item ID matches the expected value.
     *
     * <p>Calls {@code getItemId()} via reflection on the item stack.</p>
     *
     * <p>Failure message: {@code "Expected item ID <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemId(stack, "Weapon_Sword_Iron");
     * }</pre>
     *
     * @param itemStack  the item stack object (runtime type: {@code ItemStack})
     * @param expectedId the expected item identifier
     * @throws IllegalArgumentException if itemStack or expectedId is null
     * @throws AssertionFailedException if the item ID does not match
     */
    public static void assertItemId(Object itemStack, String expectedId) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        if (expectedId == null) {
            throw new IllegalArgumentException("expectedId must not be null");
        }
        String actual = invokeStringMethod(itemStack, "getItemId");
        if (!expectedId.equals(actual)) {
            HytaleAssert.fail("Expected item ID <%s> but was <%s>", expectedId, actual);
        }
    }

    /**
     * Asserts that an item stack's quantity matches the expected value.
     *
     * <p>Calls {@code getQuantity()} via reflection on the item stack.</p>
     *
     * <p>Failure message: {@code "Expected item quantity <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemQuantity(stack, 5);
     * }</pre>
     *
     * @param itemStack        the item stack object (runtime type: {@code ItemStack})
     * @param expectedQuantity the expected quantity
     * @throws IllegalArgumentException if itemStack is null
     * @throws AssertionFailedException if the quantity does not match
     */
    public static void assertItemQuantity(Object itemStack, int expectedQuantity) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        int actual = invokeIntMethod(itemStack, "getQuantity");
        if (actual != expectedQuantity) {
            HytaleAssert.fail("Expected item quantity <%d> but was <%d>", expectedQuantity, actual);
        }
    }

    /**
     * Asserts that an item stack is not empty.
     *
     * <p>Calls {@code isEmpty()} via reflection and asserts the result is {@code false}.</p>
     *
     * <p>Failure message: {@code "Expected item stack to not be empty but isEmpty() returned true"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemNotEmpty(stack);
     * }</pre>
     *
     * @param itemStack the item stack object (runtime type: {@code ItemStack})
     * @throws IllegalArgumentException if itemStack is null
     * @throws AssertionFailedException if the item stack is empty
     */
    public static void assertItemNotEmpty(Object itemStack) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        boolean empty = invokeBooleanMethod(itemStack, "isEmpty");
        if (empty) {
            HytaleAssert.fail("Expected item stack to not be empty but isEmpty() returned true");
        }
    }

    /**
     * Asserts that an item stack is empty.
     *
     * <p>Calls {@code isEmpty()} via reflection and asserts the result is {@code true}.</p>
     *
     * <p>Failure message: {@code "Expected item stack to be empty but isEmpty() returned false"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemEmpty(stack);
     * }</pre>
     *
     * @param itemStack the item stack object (runtime type: {@code ItemStack})
     * @throws IllegalArgumentException if itemStack is null
     * @throws AssertionFailedException if the item stack is not empty
     */
    public static void assertItemEmpty(Object itemStack) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        boolean empty = invokeBooleanMethod(itemStack, "isEmpty");
        if (!empty) {
            HytaleAssert.fail("Expected item stack to be empty but isEmpty() returned false");
        }
    }

    /**
     * Asserts that an item stack is broken.
     *
     * <p>Calls {@code isBroken()} via reflection and asserts the result is {@code true}.</p>
     *
     * <p>Failure message: {@code "Expected item to be broken but isBroken() returned false"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemBroken(stack);
     * }</pre>
     *
     * @param itemStack the item stack object (runtime type: {@code ItemStack})
     * @throws IllegalArgumentException if itemStack is null
     * @throws AssertionFailedException if the item is not broken
     */
    public static void assertItemBroken(Object itemStack) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        boolean broken = invokeBooleanMethod(itemStack, "isBroken");
        if (!broken) {
            HytaleAssert.fail("Expected item to be broken but isBroken() returned false");
        }
    }

    /**
     * Asserts that an item stack is not broken.
     *
     * <p>Calls {@code isBroken()} via reflection and asserts the result is {@code false}.</p>
     *
     * <p>Failure message: {@code "Expected item to not be broken but isBroken() returned true"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemNotBroken(stack);
     * }</pre>
     *
     * @param itemStack the item stack object (runtime type: {@code ItemStack})
     * @throws IllegalArgumentException if itemStack is null
     * @throws AssertionFailedException if the item is broken
     */
    public static void assertItemNotBroken(Object itemStack) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        boolean broken = invokeBooleanMethod(itemStack, "isBroken");
        if (broken) {
            HytaleAssert.fail("Expected item to not be broken but isBroken() returned true");
        }
    }

    /**
     * Asserts that an item stack's durability is within the expected tolerance.
     *
     * <p>Calls {@code getDurability()} via reflection and checks that the actual value
     * is within {@code tolerance} of the expected value.</p>
     *
     * <p>Failure message: {@code "Expected item durability <expected> (tolerance <tolerance>) but was <actual>"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemDurability(stack, 80.0, 1.0);
     * }</pre>
     *
     * @param itemStack the item stack object (runtime type: {@code ItemStack})
     * @param expected  the expected durability value
     * @param tolerance the maximum acceptable deviation
     * @throws IllegalArgumentException if itemStack is null
     * @throws AssertionFailedException if the durability is outside the tolerance
     */
    public static void assertItemDurability(Object itemStack, double expected, double tolerance) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        double actual = invokeDoubleMethod(itemStack, "getDurability");
        if (Math.abs(expected - actual) > tolerance) {
            HytaleAssert.fail("Expected item durability <%s> (tolerance %s) but was <%s>",
                    expected, tolerance, actual);
        }
    }

    /**
     * Asserts that two item stacks are stackable with each other.
     *
     * <p>Calls {@code isStackableWith(ItemStack)} via reflection on {@code stackA},
     * passing {@code stackB} as the argument.</p>
     *
     * <p>Failure message: {@code "Expected item stacks to be stackable but isStackableWith() returned false"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemStackable(stackA, stackB);
     * }</pre>
     *
     * @param stackA the first item stack object (runtime type: {@code ItemStack})
     * @param stackB the second item stack object (runtime type: {@code ItemStack})
     * @throws IllegalArgumentException if stackA or stackB is null
     * @throws AssertionFailedException if the stacks are not stackable
     */
    public static void assertItemStackable(Object stackA, Object stackB) {
        if (stackA == null) {
            throw new IllegalArgumentException("stackA must not be null");
        }
        if (stackB == null) {
            throw new IllegalArgumentException("stackB must not be null");
        }
        boolean stackable = invokeIsStackableWith(stackA, stackB);
        if (!stackable) {
            HytaleAssert.fail("Expected item stacks to be stackable but isStackableWith() returned false");
        }
    }

    /**
     * Asserts that an item stack's metadata contains a key with the expected value.
     *
     * <p>Retrieves the metadata BSON document via {@code getMetadata()} on the item stack,
     * then looks up the given key and compares the value.</p>
     *
     * <p>Failure message: {@code "Expected metadata key '<key>' to be <expected> but was <actual>"} or
     * {@code "Expected metadata key '<key>' to exist but metadata was null"}</p>
     *
     * <pre>{@code
     * ItemAssert.assertItemMetadata(stack, "enchantment", "fire_aspect");
     * }</pre>
     *
     * @param itemStack     the item stack object (runtime type: {@code ItemStack})
     * @param key           the metadata key to look up
     * @param expectedValue the expected value for the key
     * @throws IllegalArgumentException if itemStack, key, or expectedValue is null
     * @throws AssertionFailedException if the metadata key is absent or value does not match
     */
    public static void assertItemMetadata(Object itemStack, String key, Object expectedValue) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack must not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (expectedValue == null) {
            throw new IllegalArgumentException("expectedValue must not be null");
        }
        Object metadata = invokeObjectMethod(itemStack, "getMetadata");
        if (metadata == null) {
            HytaleAssert.fail("Expected metadata key '%s' to exist but metadata was null", key);
        }
        Object actualValue = invokeGetOnDocument(metadata, key);
        if (actualValue == null) {
            HytaleAssert.fail("Expected metadata key '%s' to be <%s> but key was not found",
                    key, expectedValue);
        }
        if (!expectedValue.equals(actualValue) && !expectedValue.toString().equals(actualValue.toString())) {
            HytaleAssert.fail("Expected metadata key '%s' to be <%s> but was <%s>",
                    key, expectedValue, actualValue);
        }
    }

    private static boolean invokeIsStackableWith(Object stackA, Object stackB) {
        try {
            for (Method method : stackA.getClass().getMethods()) {
                if ("isStackableWith".equals(method.getName()) && method.getParameterCount() == 1) {
                    Object result = method.invoke(stackA, stackB);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke isStackableWith(): %s", e.getMessage());
        }
        HytaleAssert.fail("ItemStack does not have isStackableWith(ItemStack) method");
        return false;
    }

    private static Object invokeGetOnDocument(Object document, String key) {
        try {
            for (Method method : document.getClass().getMethods()) {
                if ("get".equals(method.getName()) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType == String.class || paramType == Object.class) {
                        return method.invoke(document, key);
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to look up metadata key '%s': %s", key, e.getMessage());
        }
        HytaleAssert.fail("Metadata document does not have get(String) method");
        return null;
    }

    private static String invokeStringMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    return result != null ? result.toString() : null;
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return null;
    }

    private static int invokeIntMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    if (result instanceof Number) {
                        return ((Number) result).intValue();
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return 0;
    }

    private static double invokeDoubleMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    if (result instanceof Number) {
                        return ((Number) result).doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return 0.0;
    }

    private static boolean invokeBooleanMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
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

    private static Object invokeObjectMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    return method.invoke(target);
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return null;
    }
}
