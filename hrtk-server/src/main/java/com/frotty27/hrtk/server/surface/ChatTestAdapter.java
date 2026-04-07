package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;

public final class ChatTestAdapter {

    private static final String UNIVERSE_CLASS = "com.hypixel.hytale.server.core.universe.Universe";

    private ChatTestAdapter() {}

    public static boolean broadcastMessage(String message) {
        try {
            if (message == null) return false;
            Class<?> universeClass = Class.forName(UNIVERSE_CLASS);
            Method getMethod = universeClass.getMethod("get");
            Object universe = getMethod.invoke(null);
            if (universe == null) return false;
            for (Method m : universe.getClass().getMethods()) {
                if ("sendMessage".equals(m.getName()) && m.getParameterCount() == 1) {
                    m.invoke(universe, message);
                    return true;
                }
                if ("broadcastMessage".equals(m.getName()) && m.getParameterCount() == 1) {
                    m.invoke(universe, message);
                    return true;
                }
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }

    public static boolean chatEventClassAvailable() {
        try {
            Class.forName("com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent");
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}
