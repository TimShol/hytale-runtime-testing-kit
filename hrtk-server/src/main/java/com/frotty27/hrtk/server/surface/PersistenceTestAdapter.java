package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;
import java.nio.file.Path;

public final class PersistenceTestAdapter {

    private static final String UNIVERSE_CLASS = "com.hypixel.hytale.server.core.universe.Universe";
    private static final String PLAYER_STORAGE_CLASS = "com.hypixel.hytale.server.core.universe.playerdata.PlayerStorage";

    private PersistenceTestAdapter() {}

    public static Object getPlayerStorage() {
        try {
            Class<?> universeClass = Class.forName(UNIVERSE_CLASS);
            Method getMethod = universeClass.getMethod("get");
            Object universe = getMethod.invoke(null);
            if (universe == null) return null;
            for (Method m : universe.getClass().getMethods()) {
                if ("getPlayerStorage".equals(m.getName()) && m.getParameterCount() == 0) {
                    return m.invoke(universe);
                }
                if ("getPlayerStorageProvider".equals(m.getName()) && m.getParameterCount() == 0) {
                    return m.invoke(universe);
                }
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }

    public static Path getWorldSavePath(Object world) {
        try {
            if (world == null) return null;
            for (Method m : world.getClass().getMethods()) {
                if ("getSavePath".equals(m.getName()) && m.getParameterCount() == 0) {
                    Object result = m.invoke(world);
                    if (result instanceof Path p) return p;
                    return result != null ? Path.of(result.toString()) : null;
                }
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }

    public static boolean playerStorageAvailable() {
        try {
            Class.forName(PLAYER_STORAGE_CLASS);
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}
