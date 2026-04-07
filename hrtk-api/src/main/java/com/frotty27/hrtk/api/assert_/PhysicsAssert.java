package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for physics mechanics - velocity, speed, ground state, and motion.
 *
 * <p>All methods accept {@code Object} for store and ref to avoid coupling the
 * API module to HytaleServer.jar. At runtime, these map to
 * {@code Store<EntityStore>} and {@code Ref<EntityStore>}. Velocity data is accessed
 * reflectively through the {@code com.hypixel.hytale.server.core.modules.physics.Velocity}
 * component.</p>
 *
 * <pre>{@code
 * PhysicsAssert.assertVelocity(store, ref, 0.0, -9.8, 0.0, 0.5);
 * PhysicsAssert.assertSpeed(store, ref, 0.0, 5.0);
 * PhysicsAssert.assertOnGround(store, ref);
 * PhysicsAssert.assertInAir(store, ref);
 * PhysicsAssert.assertStationary(store, ref, 0.01);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class PhysicsAssert {

    private PhysicsAssert() {}

    /**
     * Asserts that an entity's velocity components match the expected values within tolerance.
     *
     * <p>Retrieves the {@code Velocity} component from the entity and checks the
     * x, y, and z components individually via reflection.</p>
     *
     * <p>Failure message: {@code "Expected velocity (<eX>, <eY>, <eZ>) but was (<aX>, <aY>, <aZ>) - tolerance <tol>"}</p>
     *
     * <pre>{@code
     * PhysicsAssert.assertVelocity(store, ref, 0.0, -9.8, 0.0, 0.5);
     * }</pre>
     *
     * @param store     the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref       the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param expectedX the expected x velocity component
     * @param expectedY the expected y velocity component
     * @param expectedZ the expected z velocity component
     * @param tolerance the maximum acceptable deviation per axis
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if any velocity component differs beyond tolerance
     */
    public static void assertVelocity(Object store, Object ref,
                                      double expectedX, double expectedY, double expectedZ,
                                      double tolerance) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        Object velocity = getVelocityComponent(store, ref);
        double actualX = invokeDoubleGetter(velocity, "x", "getX");
        double actualY = invokeDoubleGetter(velocity, "y", "getY");
        double actualZ = invokeDoubleGetter(velocity, "z", "getZ");
        if (Math.abs(expectedX - actualX) > tolerance ||
                Math.abs(expectedY - actualY) > tolerance ||
                Math.abs(expectedZ - actualZ) > tolerance) {
            HytaleAssert.fail(
                    "Expected velocity (%s, %s, %s) but was (%s, %s, %s) - tolerance %s",
                    expectedX, expectedY, expectedZ, actualX, actualY, actualZ, tolerance);
        }
    }

    /**
     * Asserts that an entity's speed is within the given range (inclusive).
     *
     * <p>Retrieves the {@code Velocity} component and calls {@code getSpeed()} via
     * reflection. If {@code getSpeed()} is unavailable, the speed is computed from
     * the x, y, and z components.</p>
     *
     * <p>Failure message: {@code "Expected speed between <min> and <max> but was <actual>"}</p>
     *
     * <pre>{@code
     * PhysicsAssert.assertSpeed(store, ref, 0.0, 5.0);
     * }</pre>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param minSpeed the minimum acceptable speed (inclusive)
     * @param maxSpeed the maximum acceptable speed (inclusive)
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if speed is outside the range
     */
    public static void assertSpeed(Object store, Object ref, double minSpeed, double maxSpeed) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        Object velocity = getVelocityComponent(store, ref);
        double speed = getSpeed(velocity);
        if (speed < minSpeed || speed > maxSpeed) {
            HytaleAssert.fail("Expected speed between <%s> and <%s> but was <%s>",
                    minSpeed, maxSpeed, speed);
        }
    }

    /**
     * Asserts that an entity is on the ground.
     *
     * <p>Checks the ground state via the entity's physics-related components.
     * Tries {@code isOnGround()} on the Velocity component or a
     * {@code PhysicsValues} component if available.</p>
     *
     * <p>Failure message: {@code "Expected entity to be on ground but it was not"}</p>
     *
     * <pre>{@code
     * PhysicsAssert.assertOnGround(store, ref);
     * }</pre>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if the entity is not on the ground
     */
    public static void assertOnGround(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        boolean onGround = checkOnGround(store, ref);
        if (!onGround) {
            HytaleAssert.fail("Expected entity to be on ground but it was not");
        }
    }

    /**
     * Asserts that an entity is in the air (not on the ground).
     *
     * <p>Failure message: {@code "Expected entity to be in the air but it was on ground"}</p>
     *
     * <pre>{@code
     * PhysicsAssert.assertInAir(store, ref);
     * }</pre>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if the entity is on the ground
     */
    public static void assertInAir(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        boolean onGround = checkOnGround(store, ref);
        if (onGround) {
            HytaleAssert.fail("Expected entity to be in the air but it was on ground");
        }
    }

    /**
     * Asserts that an entity is stationary (speed near zero within tolerance).
     *
     * <p>Retrieves the {@code Velocity} component, computes the speed, and checks
     * that it is less than or equal to the given tolerance.</p>
     *
     * <p>Failure message: {@code "Expected entity to be stationary (tolerance <tol>) but speed was <actual>"}</p>
     *
     * <pre>{@code
     * PhysicsAssert.assertStationary(store, ref, 0.01);
     * }</pre>
     *
     * @param store     the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref       the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param tolerance the maximum acceptable speed to be considered stationary
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if the entity's speed exceeds the tolerance
     */
    public static void assertStationary(Object store, Object ref, double tolerance) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        Object velocity = getVelocityComponent(store, ref);
        double speed = getSpeed(velocity);
        if (speed > tolerance) {
            HytaleAssert.fail("Expected entity to be stationary (tolerance %s) but speed was <%s>",
                    tolerance, speed);
        }
    }

    private static Object getVelocityComponent(Object store, Object ref) {
        Object component = getComponentByName(store, ref, "Velocity");
        if (component == null) {
            HytaleAssert.fail("Entity does not have a Velocity component");
        }
        return component;
    }

    private static double getSpeed(Object velocity) {
        try {
            for (Method method : velocity.getClass().getMethods()) {
                if ("getSpeed".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(velocity);
                    if (result instanceof Number) {
                        return ((Number) result).doubleValue();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        double x = invokeDoubleGetter(velocity, "x", "getX");
        double y = invokeDoubleGetter(velocity, "y", "getY");
        double z = invokeDoubleGetter(velocity, "z", "getZ");
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static boolean checkOnGround(Object store, Object ref) {
        Object velocity = getComponentByName(store, ref, "Velocity");
        if (velocity != null) {
            Boolean result = tryInvokeBooleanMethod(velocity, "isOnGround");
            if (result != null) {
                return result;
            }
        }
        Object physicsValues = getComponentByName(store, ref, "PhysicsValues");
        if (physicsValues != null) {
            Boolean result = tryInvokeBooleanMethod(physicsValues, "isOnGround");
            if (result != null) {
                return result;
            }
        }
        Object groundState = getComponentByName(store, ref, "GroundState");
        if (groundState != null) {
            Boolean result = tryInvokeBooleanMethod(groundState, "isOnGround");
            if (result != null) {
                return result;
            }
        }
        HytaleAssert.fail("Could not determine ground state - no isOnGround() method found on "
                + "Velocity, PhysicsValues, or GroundState components");
        return false;
    }

    private static double invokeDoubleGetter(Object target, String fieldName, String getterName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (getterName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    if (result instanceof Number) {
                        return ((Number) result).doubleValue();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        try {
            java.lang.reflect.Field field = target.getClass().getField(fieldName);
            Object result = field.get(target);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
        } catch (Exception ignored) {
        }
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object result = field.get(target);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
        } catch (Exception ignored) {
        }
        HytaleAssert.fail("Could not read '%s' from %s", fieldName, target.getClass().getSimpleName());
        return 0.0;
    }

    private static Boolean tryInvokeBooleanMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Object getComponentByName(Object store, Object ref, String componentSimpleName) {
        try {
            for (Method method : store.getClass().getMethods()) {
                if ("getComponent".equals(method.getName()) && method.getParameterCount() == 2) {
                    for (Method m : store.getClass().getMethods()) {
                        if ("getComponentTypes".equals(m.getName()) && m.getParameterCount() == 0) {
                            Object types = m.invoke(store);
                            if (types instanceof Iterable<?>) {
                                for (Object type : (Iterable<?>) types) {
                                    if (type.toString().contains(componentSimpleName) ||
                                            type.getClass().getSimpleName().contains(componentSimpleName)) {
                                        Object comp = method.invoke(store, ref, type);
                                        if (comp != null) {
                                            return comp;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    for (Method m : store.getClass().getMethods()) {
                        if ("getComponents".equals(m.getName()) && m.getParameterCount() == 1) {
                            Object components = m.invoke(store, ref);
                            if (components instanceof Iterable<?>) {
                                for (Object comp : (Iterable<?>) components) {
                                    if (comp.getClass().getSimpleName().contains(componentSimpleName)) {
                                        return comp;
                                    }
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to look up component '%s': %s", componentSimpleName, e.getMessage());
        }
        return null;
    }
}
