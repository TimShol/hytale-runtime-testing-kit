package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;

public final class WeatherTestAdapter {

    private static final String WORLD_CONFIG_CLASS = "com.hypixel.hytale.server.core.universe.world.WorldConfig";
    private static final String WEATHER_PLUGIN = "com.hypixel.hytale.builtin.weather.WeatherPlugin";

    private WeatherTestAdapter() {}

    public static String getWeather(Object world) {
        try {
            if (world == null) return null;
            Method getConfig = world.getClass().getMethod("getConfig");
            Object config = getConfig.invoke(world);
            if (config == null) return null;
            for (Method m : config.getClass().getMethods()) {
                if ("getForcedWeather".equals(m.getName()) || "getWeather".equals(m.getName())
                        || "getCurrentWeather".equals(m.getName())) {
                    Object result = m.invoke(config);
                    return result != null ? String.valueOf(result) : null;
                }
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }

    public static boolean setWeather(Object world, String weatherId) {
        try {
            if (world == null || weatherId == null) return false;
            Method getConfig = world.getClass().getMethod("getConfig");
            Object config = getConfig.invoke(world);
            if (config == null) return false;
            for (Method m : config.getClass().getMethods()) {
                if ("setForcedWeather".equals(m.getName()) && m.getParameterCount() == 1) {
                    m.invoke(config, weatherId);
                    return true;
                }
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }

    public static boolean weatherPluginAvailable() {
        try {
            Class<?> pluginClass = Class.forName(WEATHER_PLUGIN);
            return pluginClass != null;
        } catch (Exception _) {
            return false;
        }
    }
}
