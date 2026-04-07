package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for weather state - checking forced weather on a world.
 *
 * <p>Uses reflection to access {@code WorldConfig} weather fields without importing
 * HytaleServer.jar classes directly.</p>
 *
 * <pre>{@code
 * WeatherAssert.assertWeather(world, "Rain");
 * WeatherAssert.assertNotWeather(world, "Clear");
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class WeatherAssert {

    private WeatherAssert() {}

    /**
     * Asserts that the world's current weather matches the expected weather ID.
     *
     * <p>Uses reflection to retrieve the weather from the world's configuration
     * or weather manager.</p>
     *
     * <p>Failure message: {@code "Expected weather '<expectedWeatherId>' but was '<actual>'"}</p>
     *
     * @param world             the world object (runtime type: {@code World})
     * @param expectedWeatherId the expected weather identifier
     * @throws IllegalArgumentException    if world or expectedWeatherId is null
     * @throws AssertionFailedException    if the weather does not match the expected value
     */
    public static void assertWeather(Object world, String expectedWeatherId) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        if (expectedWeatherId == null) {
            throw new IllegalArgumentException("expectedWeatherId must not be null");
        }
        String actual = getCurrentWeather(world);
        if (!expectedWeatherId.equals(actual)) {
            HytaleAssert.fail("Expected weather '%s' but was '%s'", expectedWeatherId, actual);
        }
    }

    /**
     * Asserts that the world's current weather does not match the given weather ID.
     *
     * <p>Uses reflection to retrieve the weather from the world's configuration
     * or weather manager.</p>
     *
     * <p>Failure message: {@code "Expected weather to not be '<unexpectedWeatherId>' but it was"}</p>
     *
     * @param world               the world object (runtime type: {@code World})
     * @param unexpectedWeatherId the weather identifier that should not be active
     * @throws IllegalArgumentException    if world or unexpectedWeatherId is null
     * @throws AssertionFailedException    if the weather matches the unexpected value
     */
    public static void assertNotWeather(Object world, String unexpectedWeatherId) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        if (unexpectedWeatherId == null) {
            throw new IllegalArgumentException("unexpectedWeatherId must not be null");
        }
        String actual = getCurrentWeather(world);
        if (unexpectedWeatherId.equals(actual)) {
            HytaleAssert.fail("Expected weather to not be '%s' but it was", unexpectedWeatherId);
        }
    }

    private static String getCurrentWeather(Object world) {
        try {
            for (Method method : world.getClass().getMethods()) {
                if (("getWeather".equals(method.getName()) || "getCurrentWeather".equals(method.getName())
                        || "getWeatherId".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(world);
                    if (result != null) {
                        return result.toString();
                    }
                }
            }
            Object config = getWorldConfig(world);
            if (config != null) {
                for (Method method : config.getClass().getMethods()) {
                    if (("getWeather".equals(method.getName()) || "getForcedWeather".equals(method.getName())
                            || "getWeatherId".equals(method.getName()))
                            && method.getParameterCount() == 0) {
                        Object result = method.invoke(config);
                        if (result != null) {
                            return result.toString();
                        }
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get current weather via reflection: %s", e.getMessage());
        }
        HytaleAssert.fail("Could not determine weather - no getWeather() or WorldConfig weather method found");
        return null;
    }

    private static Object getWorldConfig(Object world) {
        try {
            for (Method method : world.getClass().getMethods()) {
                if (("getConfig".equals(method.getName()) || "getWorldConfig".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    return method.invoke(world);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
