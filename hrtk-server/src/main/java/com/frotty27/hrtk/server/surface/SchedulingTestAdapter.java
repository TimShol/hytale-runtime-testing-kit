package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;

public final class SchedulingTestAdapter {

    private static final String WORLD_CLASS = "com.hypixel.hytale.server.core.universe.world.World";

    private SchedulingTestAdapter() {}

    public static boolean executeOnWorldThread(Object world, Runnable task) {
        try {
            if (world == null || task == null) return false;
            for (Method m : world.getClass().getMethods()) {
                if ("execute".equals(m.getName()) && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isAssignableFrom(Runnable.class)) {
                    m.invoke(world, task);
                    return true;
                }
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }

    public static boolean schedulingAvailable() {
        try {
            Class<?> worldClass = Class.forName(WORLD_CLASS);
            for (Method m : worldClass.getMethods()) {
                if ("execute".equals(m.getName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }
}
