package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;

public final class PermissionsTestAdapter {

    private static final String PERMISSION_HOLDER_CLASS = "com.hypixel.hytale.server.core.modules.permissions.PermissionHolder";

    private PermissionsTestAdapter() {}

    public static boolean hasPermission(Object permissionHolder, String permission) {
        try {
            if (permissionHolder == null || permission == null) return false;
            Class<?> holderClass = Class.forName(PERMISSION_HOLDER_CLASS);
            if (!holderClass.isInstance(permissionHolder)) return false;
            Method method = holderClass.getMethod("hasPermission", String.class);
            Object result = method.invoke(permissionHolder, permission);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean addPermission(Object permissionHolder, String permission) {
        try {
            if (permissionHolder == null || permission == null) return false;
            Class<?> holderClass = Class.forName(PERMISSION_HOLDER_CLASS);
            if (!holderClass.isInstance(permissionHolder)) return false;
            Method method = holderClass.getMethod("addPermission", String.class);
            method.invoke(permissionHolder, permission);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean removePermission(Object permissionHolder, String permission) {
        try {
            if (permissionHolder == null || permission == null) return false;
            Class<?> holderClass = Class.forName(PERMISSION_HOLDER_CLASS);
            if (!holderClass.isInstance(permissionHolder)) return false;
            Method method = holderClass.getMethod("removePermission", String.class);
            method.invoke(permissionHolder, permission);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
