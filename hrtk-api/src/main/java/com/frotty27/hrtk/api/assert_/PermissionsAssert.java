package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for permission checks - verifying permission holders have or lack permissions.
 *
 * <p>All methods accept {@code Object} for the permission holder to avoid coupling the
 * API module to HytaleServer.jar. At runtime, {@code permissionHolder} maps to any
 * object that exposes a {@code hasPermission(String)} method. Permissions are checked
 * reflectively.</p>
 *
 * <pre>{@code
 * PermissionsAssert.assertHasPermission(player, "hytale.admin.kick");
 * PermissionsAssert.assertNoPermission(player, "hytale.admin.ban");
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class PermissionsAssert {

    private PermissionsAssert() {}

    /**
     * Asserts that a permission holder has the specified permission.
     *
     * <p>Calls {@code hasPermission(String)} via reflection on the permission holder
     * and asserts the result is {@code true}.</p>
     *
     * <p>Failure message: {@code "Expected permission holder to have permission '<permission>' but it did not"}</p>
     *
     * <pre>{@code
     * PermissionsAssert.assertHasPermission(player, "hytale.admin.kick");
     * }</pre>
     *
     * @param permissionHolder the object that holds permissions
     * @param permission       the permission string to check
     * @throws IllegalArgumentException if permissionHolder or permission is null
     * @throws AssertionFailedException if the holder does not have the permission
     */
    public static void assertHasPermission(Object permissionHolder, String permission) {
        if (permissionHolder == null) {
            throw new IllegalArgumentException("permissionHolder must not be null");
        }
        if (permission == null) {
            throw new IllegalArgumentException("permission must not be null");
        }
        boolean has = invokeHasPermission(permissionHolder, permission);
        if (!has) {
            HytaleAssert.fail("Expected permission holder to have permission '%s' but it did not",
                    permission);
        }
    }

    /**
     * Asserts that a permission holder does not have the specified permission.
     *
     * <p>Calls {@code hasPermission(String)} via reflection on the permission holder
     * and asserts the result is {@code false}.</p>
     *
     * <p>Failure message: {@code "Expected permission holder to not have permission '<permission>' but it did"}</p>
     *
     * <pre>{@code
     * PermissionsAssert.assertNoPermission(player, "hytale.admin.ban");
     * }</pre>
     *
     * @param permissionHolder the object that holds permissions
     * @param permission       the permission string to check
     * @throws IllegalArgumentException if permissionHolder or permission is null
     * @throws AssertionFailedException if the holder has the permission
     */
    public static void assertNoPermission(Object permissionHolder, String permission) {
        if (permissionHolder == null) {
            throw new IllegalArgumentException("permissionHolder must not be null");
        }
        if (permission == null) {
            throw new IllegalArgumentException("permission must not be null");
        }
        boolean has = invokeHasPermission(permissionHolder, permission);
        if (has) {
            HytaleAssert.fail("Expected permission holder to not have permission '%s' but it did",
                    permission);
        }
    }

    private static boolean invokeHasPermission(Object holder, String permission) {
        try {
            for (Method method : holder.getClass().getMethods()) {
                if ("hasPermission".equals(method.getName()) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType == String.class || paramType == Object.class) {
                        Object result = method.invoke(holder, permission);
                        if (result instanceof Boolean) {
                            return (Boolean) result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke hasPermission('%s'): %s", permission, e.getMessage());
        }
        HytaleAssert.fail("Permission holder (%s) does not have hasPermission(String) method",
                holder.getClass().getSimpleName());
        return false;
    }
}
