package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for entity effects - status effects, invulnerability, and effect counts.
 *
 * <p>All methods accept {@code Object} for store and ref to avoid coupling the
 * API module to HytaleServer.jar. At runtime, these map to
 * {@code Store<EntityStore>} and {@code Ref<EntityStore>}. Effects are accessed
 * through the entity's {@code EffectControllerComponent}.</p>
 *
 * <pre>{@code
 * EffectAssert.assertHasEffect(store, entityRef, 3);
 * EffectAssert.assertNoEffect(store, entityRef, 5);
 * EffectAssert.assertEffectCount(store, entityRef, 2);
 * EffectAssert.assertInvulnerable(store, entityRef);
 * }</pre>
 *
 * @see StatsAssert
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class EffectAssert {

    private EffectAssert() {}

    /**
     * Asserts that an entity has the effect with the given index active.
     *
     * <p>Checks the entity's {@code EffectControllerComponent} via its
     * {@code hasEffect(int)} method.</p>
     *
     * <p>Failure message: {@code "Expected entity to have effect index N but it was absent"}</p>
     *
     * @param store       the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref         the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param effectIndex the effect index to check
     * @throws AssertionFailedException if the entity does not have the effect
     */
    public static void assertHasEffect(Object store, Object ref, int effectIndex) {
        Object controller = getEffectController(store, ref);
        boolean has = invokeHasEffect(controller, effectIndex);
        if (!has) {
            HytaleAssert.fail("Expected entity to have effect index %d but it was absent", effectIndex);
        }
    }

    /**
     * Asserts that an entity does NOT have the effect with the given index.
     *
     * <p>Failure message: {@code "Expected entity to NOT have effect index N but it was present"}</p>
     *
     * @param store       the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref         the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param effectIndex the effect index to check
     * @throws AssertionFailedException if the entity has the effect
     */
    public static void assertNoEffect(Object store, Object ref, int effectIndex) {
        Object controller = getEffectController(store, ref);
        boolean has = invokeHasEffect(controller, effectIndex);
        if (has) {
            HytaleAssert.fail("Expected entity to NOT have effect index %d but it was present", effectIndex);
        }
    }

    /**
     * Asserts that an entity has exactly the expected number of active effects.
     *
     * <p>Retrieves the active effects list from the {@code EffectControllerComponent}
     * and compares its size.</p>
     *
     * <p>Failure message: {@code "Expected N active effects but found M"}</p>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param expected the exact number of active effects expected
     * @throws AssertionFailedException if the count does not match
     */
    public static void assertEffectCount(Object store, Object ref, int expected) {
        Object controller = getEffectController(store, ref);
        int actual = getActiveEffectCount(controller);
        if (actual != expected) {
            HytaleAssert.fail("Expected %d active effects but found %d", expected, actual);
        }
    }

    /**
     * Asserts that an entity is invulnerable via its {@code EffectControllerComponent}.
     *
     * <p>Checks the {@code isInvulnerable()} method on the effect controller.</p>
     *
     * <p>Failure message: {@code "Expected entity to be invulnerable but it was not"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the entity is not invulnerable
     */
    public static void assertInvulnerable(Object store, Object ref) {
        Object controller = getEffectController(store, ref);
        boolean invulnerable = invokeIsInvulnerable(controller);
        if (!invulnerable) {
            HytaleAssert.fail("Expected entity to be invulnerable but it was not");
        }
    }

    /**
     * Asserts that the active effect at the given index has a remaining duration at or above
     * the specified minimum.
     *
     * <p>Retrieves the effect via {@code getEffect(int)} on the controller and reads
     * its {@code getRemainingDuration()} or {@code getDuration()} value.</p>
     *
     * <p>Failure message: {@code "Expected effect N remaining duration >= <min> but was <actual>"}</p>
     *
     * @param store       the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref         the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param effectIndex the effect index to check
     * @param minDuration the minimum expected remaining duration
     * @throws AssertionFailedException if the remaining duration is below the minimum
     */
    public static void assertEffectDuration(Object store, Object ref, int effectIndex, float minDuration) {
        if (store == null) {
            HytaleAssert.fail("store must not be null");
        }
        if (ref == null) {
            HytaleAssert.fail("ref must not be null");
        }
        Object controller = getEffectController(store, ref);
        Object effect = getEffectByIndex(controller, effectIndex);
        float remaining = getEffectRemainingDuration(effect, effectIndex);
        if (remaining < minDuration) {
            HytaleAssert.fail("Expected effect %d remaining duration >= <%s> but was <%s>",
                    effectIndex, minDuration, remaining);
        }
    }

    /**
     * Asserts that the active effect at the given index is a debuff.
     *
     * <p>Checks the {@code isDebuff()} method on the effect instance retrieved via
     * the entity's {@code EffectControllerComponent}.</p>
     *
     * <p>Failure message: {@code "Expected effect N to be a debuff but it was not"}</p>
     *
     * @param store       the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref         the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param effectIndex the effect index to check
     * @throws AssertionFailedException if the effect is not a debuff
     */
    public static void assertIsDebuff(Object store, Object ref, int effectIndex) {
        if (store == null) {
            HytaleAssert.fail("store must not be null");
        }
        if (ref == null) {
            HytaleAssert.fail("ref must not be null");
        }
        Object controller = getEffectController(store, ref);
        Object effect = getEffectByIndex(controller, effectIndex);
        boolean debuff = invokeBooleanMethod(effect, "isDebuff");
        if (!debuff) {
            HytaleAssert.fail("Expected effect %d to be a debuff but it was not", effectIndex);
        }
    }

    /**
     * Asserts that an entity is NOT invulnerable via its {@code EffectControllerComponent}.
     *
     * <p>Failure message: {@code "Expected entity to NOT be invulnerable but it was"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the entity is invulnerable
     */
    public static void assertNotInvulnerable(Object store, Object ref) {
        if (store == null) {
            HytaleAssert.fail("store must not be null");
        }
        if (ref == null) {
            HytaleAssert.fail("ref must not be null");
        }
        Object controller = getEffectController(store, ref);
        boolean invulnerable = invokeIsInvulnerable(controller);
        if (invulnerable) {
            HytaleAssert.fail("Expected entity to NOT be invulnerable but it was");
        }
    }

    private static Object getEffectByIndex(Object controller, int effectIndex) {
        try {
            for (Method method : controller.getClass().getMethods()) {
                if ("getEffect".equals(method.getName()) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType == int.class || paramType == Integer.class) {
                        Object effect = method.invoke(controller, effectIndex);
                        if (effect != null) {
                            return effect;
                        }
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get effect at index %d: %s", effectIndex, e.getMessage());
        }
        HytaleAssert.fail("Effect at index %d not found on EffectControllerComponent", effectIndex);
        return null;
    }

    private static float getEffectRemainingDuration(Object effect, int effectIndex) {
        try {
            for (Method method : effect.getClass().getMethods()) {
                if (("getRemainingDuration".equals(method.getName()) || "getDuration".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(effect);
                    if (result instanceof Number) {
                        return ((Number) result).floatValue();
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get remaining duration for effect %d: %s", effectIndex, e.getMessage());
        }
        HytaleAssert.fail("Effect at index %d has no getRemainingDuration() or getDuration() method", effectIndex);
        return 0f;
    }

    private static boolean invokeBooleanMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return false;
    }

    private static Object getEffectController(Object store, Object ref) {
        Object component = getComponentByName(store, ref, "EffectControllerComponent");
        if (component == null) {
            HytaleAssert.fail("Entity does not have an EffectControllerComponent");
        }
        return component;
    }

    private static boolean invokeHasEffect(Object controller, int effectIndex) {
        try {
            for (Method method : controller.getClass().getMethods()) {
                if ("hasEffect".equals(method.getName()) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType == int.class || paramType == Integer.class) {
                        Object result = method.invoke(controller, effectIndex);
                        if (result instanceof Boolean) {
                            return (Boolean) result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke hasEffect(%d): %s", effectIndex, e.getMessage());
        }
        HytaleAssert.fail("EffectControllerComponent does not have hasEffect(int) method");
        return false;
    }

    private static int getActiveEffectCount(Object controller) {
        try {
            for (Method method : controller.getClass().getMethods()) {
                if ("getActiveEffects".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object effects = method.invoke(controller);
                    if (effects instanceof java.util.Collection<?>) {
                        return ((java.util.Collection<?>) effects).size();
                    }
                    for (Method sizeMethod : effects.getClass().getMethods()) {
                        if ("size".equals(sizeMethod.getName()) && sizeMethod.getParameterCount() == 0) {
                            Object result = sizeMethod.invoke(effects);
                            if (result instanceof Number) {
                                return ((Number) result).intValue();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get active effect count: %s", e.getMessage());
        }
        HytaleAssert.fail("EffectControllerComponent does not have getActiveEffects() method");
        return 0;
    }

    private static boolean invokeIsInvulnerable(Object controller) {
        try {
            for (Method method : controller.getClass().getMethods()) {
                if ("isInvulnerable".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(controller);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke isInvulnerable(): %s", e.getMessage());
        }
        HytaleAssert.fail("EffectControllerComponent does not have isInvulnerable() method");
        return false;
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
