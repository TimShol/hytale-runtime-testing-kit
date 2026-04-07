package com.frotty27.hrtk.server.surface;

public final class ProjectileTestAdapter {

    private static final String PROJECTILE_MODULE = "com.hypixel.hytale.server.core.modules.projectile.ProjectileModule";

    private ProjectileTestAdapter() {}

    public static boolean projectileModuleExists() {
        try {
            Class<?> moduleClass = Class.forName(PROJECTILE_MODULE);
            return moduleClass != null;
        } catch (Exception _) {
            return false;
        }
    }

    public static Object getProjectileModule() {
        try {
            Class<?> moduleClass = Class.forName(PROJECTILE_MODULE);
            for (var m : moduleClass.getMethods()) {
                if ("get".equals(m.getName()) && m.getParameterCount() == 0) {
                    return m.invoke(null);
                }
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }
}
