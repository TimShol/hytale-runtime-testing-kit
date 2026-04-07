package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for scheduling and world lifecycle state.
 *
 * <p>Uses reflection to check world liveness without importing HytaleServer.jar
 * classes directly.</p>
 *
 * <pre>{@code
 * SchedulingAssert.assertWorldAlive(world);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class SchedulingAssert {

    private SchedulingAssert() {}

    /**
     * Asserts that the given world is alive (actively running).
     *
     * <p>Uses reflection to call {@code isAlive()} on the world object.</p>
     *
     * <p>Failure message: {@code "Expected world to be alive but it was not"}</p>
     *
     * @param world the world object (runtime type: {@code World})
     * @throws IllegalArgumentException    if world is null
     * @throws AssertionFailedException    if the world is not alive or the method cannot be found
     */
    public static void assertWorldAlive(Object world) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        boolean alive = checkWorldAlive(world);
        if (!alive) {
            HytaleAssert.fail("Expected world to be alive but it was not");
        }
    }

    private static boolean checkWorldAlive(Object world) {
        try {
            for (Method method : world.getClass().getMethods()) {
                if ("isAlive".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(world);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
            for (Method method : world.getClass().getMethods()) {
                if (("isRunning".equals(method.getName()) || "isActive".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(world);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to check world alive state via reflection: %s", e.getMessage());
        }
        HytaleAssert.fail("Could not determine world alive state - no isAlive() or isRunning() method found");
        return false;
    }
}
