package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for block types - material, trigger state, default state key, and group.
 *
 * <p>All methods accept {@code Object} for block types to avoid coupling the
 * API module to HytaleServer.jar. At runtime, {@code blockType} maps to a
 * {@code BlockType} instance. Properties are accessed reflectively.</p>
 *
 * <pre>{@code
 * BlockAssert.assertBlockMaterial(blockType, "stone");
 * BlockAssert.assertBlockIsTrigger(blockType);
 * BlockAssert.assertBlockState(blockType, "default");
 * BlockAssert.assertBlockGroup(blockType, "natural");
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class BlockAssert {

    private BlockAssert() {}

    /**
     * Asserts that a block type's material matches the expected value.
     *
     * <p>Calls {@code getMaterial()} via reflection on the block type and compares
     * the result's string representation to the expected material.</p>
     *
     * <p>Failure message: {@code "Expected block material <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * BlockAssert.assertBlockMaterial(blockType, "stone");
     * }</pre>
     *
     * @param blockType        the block type object (runtime type: {@code BlockType})
     * @param expectedMaterial the expected material name
     * @throws IllegalArgumentException if blockType or expectedMaterial is null
     * @throws AssertionFailedException if the material does not match
     */
    public static void assertBlockMaterial(Object blockType, String expectedMaterial) {
        if (blockType == null) {
            throw new IllegalArgumentException("blockType must not be null");
        }
        if (expectedMaterial == null) {
            throw new IllegalArgumentException("expectedMaterial must not be null");
        }
        String actual = invokeStringMethod(blockType, "getMaterial");
        if (!expectedMaterial.equals(actual)) {
            HytaleAssert.fail("Expected block material <%s> but was <%s>", expectedMaterial, actual);
        }
    }

    /**
     * Asserts that a block type is a trigger block.
     *
     * <p>Calls {@code isTrigger()} via reflection and asserts the result is {@code true}.</p>
     *
     * <p>Failure message: {@code "Expected block to be a trigger but isTrigger() returned false"}</p>
     *
     * <pre>{@code
     * BlockAssert.assertBlockIsTrigger(blockType);
     * }</pre>
     *
     * @param blockType the block type object (runtime type: {@code BlockType})
     * @throws IllegalArgumentException if blockType is null
     * @throws AssertionFailedException if the block is not a trigger
     */
    public static void assertBlockIsTrigger(Object blockType) {
        if (blockType == null) {
            throw new IllegalArgumentException("blockType must not be null");
        }
        boolean trigger = invokeBooleanMethod(blockType, "isTrigger");
        if (!trigger) {
            HytaleAssert.fail("Expected block to be a trigger but isTrigger() returned false");
        }
    }

    /**
     * Asserts that a block type is not a trigger block.
     *
     * <p>Calls {@code isTrigger()} via reflection and asserts the result is {@code false}.</p>
     *
     * <p>Failure message: {@code "Expected block to not be a trigger but isTrigger() returned true"}</p>
     *
     * <pre>{@code
     * BlockAssert.assertBlockNotTrigger(blockType);
     * }</pre>
     *
     * @param blockType the block type object (runtime type: {@code BlockType})
     * @throws IllegalArgumentException if blockType is null
     * @throws AssertionFailedException if the block is a trigger
     */
    public static void assertBlockNotTrigger(Object blockType) {
        if (blockType == null) {
            throw new IllegalArgumentException("blockType must not be null");
        }
        boolean trigger = invokeBooleanMethod(blockType, "isTrigger");
        if (trigger) {
            HytaleAssert.fail("Expected block to not be a trigger but isTrigger() returned true");
        }
    }

    /**
     * Asserts that a block type's default state key matches the expected value.
     *
     * <p>Calls {@code getDefaultStateKey()} via reflection on the block type.</p>
     *
     * <p>Failure message: {@code "Expected block default state <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * BlockAssert.assertBlockState(blockType, "default");
     * }</pre>
     *
     * @param blockType     the block type object (runtime type: {@code BlockType})
     * @param expectedState the expected default state key
     * @throws IllegalArgumentException if blockType or expectedState is null
     * @throws AssertionFailedException if the default state key does not match
     */
    public static void assertBlockState(Object blockType, String expectedState) {
        if (blockType == null) {
            throw new IllegalArgumentException("blockType must not be null");
        }
        if (expectedState == null) {
            throw new IllegalArgumentException("expectedState must not be null");
        }
        String actual = invokeStringMethod(blockType, "getDefaultStateKey");
        if (!expectedState.equals(actual)) {
            HytaleAssert.fail("Expected block default state <%s> but was <%s>", expectedState, actual);
        }
    }

    /**
     * Asserts that a block type's group matches the expected value.
     *
     * <p>Calls {@code getGroup()} via reflection on the block type.</p>
     *
     * <p>Failure message: {@code "Expected block group <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * BlockAssert.assertBlockGroup(blockType, "natural");
     * }</pre>
     *
     * @param blockType     the block type object (runtime type: {@code BlockType})
     * @param expectedGroup the expected group name
     * @throws IllegalArgumentException if blockType or expectedGroup is null
     * @throws AssertionFailedException if the group does not match
     */
    public static void assertBlockGroup(Object blockType, String expectedGroup) {
        if (blockType == null) {
            throw new IllegalArgumentException("blockType must not be null");
        }
        if (expectedGroup == null) {
            throw new IllegalArgumentException("expectedGroup must not be null");
        }
        String actual = invokeStringMethod(blockType, "getGroup");
        if (!expectedGroup.equals(actual)) {
            HytaleAssert.fail("Expected block group <%s> but was <%s>", expectedGroup, actual);
        }
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
}
