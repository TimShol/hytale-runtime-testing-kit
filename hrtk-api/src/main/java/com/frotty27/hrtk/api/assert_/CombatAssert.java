package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for combat mechanics - damage, death, health thresholds, and knockback.
 *
 * <p>All methods accept {@code Object} for store, ref, and component types to avoid
 * coupling the API module to HytaleServer.jar. Delegates to {@link StatsAssert}
 * for health and alive/dead checks where applicable.</p>
 *
 * <pre>{@code
 * CombatAssert.assertAlive(store, entityRef);
 * CombatAssert.assertHealthBelow(store, entityRef, 10.0f);
 * CombatAssert.assertHasKnockback(store, entityRef);
 * }</pre>
 *
 * @see StatsAssert
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class CombatAssert {

    private CombatAssert() {}

    /**
     * Asserts that an entity is dead (has a {@code DeathComponent}).
     *
     * <p>Delegates to {@link StatsAssert#assertDead(Object, Object)}.</p>
     *
     * <p>Failure message: {@code "Expected entity to be dead (have DeathComponent) but it does not"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the entity does not have a DeathComponent
     * @see StatsAssert#assertDead(Object, Object)
     */
    public static void assertDead(Object store, Object ref) {
        StatsAssert.assertDead(store, ref);
    }

    /**
     * Asserts that an entity is alive (health greater than 0 and no {@code DeathComponent}).
     *
     * <p>Delegates to {@link StatsAssert#assertAlive(Object, Object)}.</p>
     *
     * <p>Failure message: {@code "Expected entity to be alive but health was <health>"} or
     * {@code "Expected entity to be alive but it has a DeathComponent"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the entity is dead
     * @see StatsAssert#assertAlive(Object, Object)
     */
    public static void assertAlive(Object store, Object ref) {
        StatsAssert.assertAlive(store, ref);
    }

    /**
     * Asserts that an entity's health equals the expected value.
     *
     * <p>Delegates to {@link StatsAssert#assertHealthEquals(Object, Object, float)}.</p>
     *
     * <p>Failure message: {@code "Expected health to be <expected> but was <actual>"}</p>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param expected the expected health value
     * @throws AssertionFailedException if the health differs from expected by more than 0.01
     * @see StatsAssert#assertHealthEquals(Object, Object, float)
     */
    public static void assertHealthEquals(Object store, Object ref, float expected) {
        StatsAssert.assertHealthEquals(store, ref, expected);
    }

    /**
     * Asserts that an entity's health is strictly below the given threshold.
     *
     * <p>Failure message: {@code "Expected health below <threshold> but was <actual>"}</p>
     *
     * @param store     the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref       the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param threshold the upper bound (exclusive) for health
     * @throws AssertionFailedException if health is greater than or equal to the threshold
     */
    public static void assertHealthBelow(Object store, Object ref, float threshold) {
        float health = getHealthValue(store, ref);
        if (health >= threshold) {
            HytaleAssert.fail("Expected health below <%s> but was <%s>", threshold, health);
        }
    }

    /**
     * Asserts that an entity currently has a {@code KnockbackComponent} attached,
     * indicating it is being knocked back.
     *
     * <p>Failure message: {@code "Expected entity to have KnockbackComponent but it was absent"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the entity does not have a KnockbackComponent
     */
    public static void assertHasKnockback(Object store, Object ref) {
        if (!hasComponentByName(store, ref, "KnockbackComponent")) {
            HytaleAssert.fail("Expected entity to have KnockbackComponent but it was absent");
        }
    }

    /**
     * Asserts that the death component's cause matches the expected cause string.
     *
     * <p>Retrieves the cause via {@code getCause()} or {@code getDamageCause()} on the
     * death component, then compares its string representation against the expected value.</p>
     *
     * <p>Failure message: {@code "Expected death cause '<expected>' but was '<actual>'"}</p>
     *
     * @param deathComponent the death component object (runtime type: {@code DeathComponent})
     * @param expectedCause  the expected cause string
     * @throws AssertionFailedException if the death component is null or the cause does not match
     */
    public static void assertDamageCause(Object deathComponent, String expectedCause) {
        if (deathComponent == null) {
            HytaleAssert.fail("deathComponent must not be null");
        }
        if (expectedCause == null) {
            HytaleAssert.fail("expectedCause must not be null");
        }
        String actual = getDeathCause(deathComponent);
        if (!expectedCause.equals(actual)) {
            HytaleAssert.fail("Expected death cause '%s' but was '%s'", expectedCause, actual);
        }
    }

    /**
     * Asserts that an entity's health is strictly above the given threshold.
     *
     * <p>Delegates to {@link StatsAssert} to retrieve the current health value.</p>
     *
     * <p>Failure message: {@code "Expected health above <threshold> but was <actual>"}</p>
     *
     * @param store     the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref       the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param threshold the lower bound (exclusive) for health
     * @throws AssertionFailedException if health is less than or equal to the threshold
     */
    public static void assertHealthAbove(Object store, Object ref, float threshold) {
        if (store == null) {
            HytaleAssert.fail("store must not be null");
        }
        if (ref == null) {
            HytaleAssert.fail("ref must not be null");
        }
        float health = getHealthValue(store, ref);
        if (health <= threshold) {
            HytaleAssert.fail("Expected health above <%s> but was <%s>", threshold, health);
        }
    }

    private static String getDeathCause(Object deathComponent) {
        try {
            for (Method method : deathComponent.getClass().getMethods()) {
                if (("getCause".equals(method.getName()) || "getDamageCause".equals(method.getName()))
                        && method.getParameterCount() == 0) {
                    Object cause = method.invoke(deathComponent);
                    if (cause != null) {
                        return cause.toString();
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get death cause via reflection: %s", e.getMessage());
        }
        HytaleAssert.fail("Could not retrieve cause from DeathComponent - no getCause() or getDamageCause() method found");
        return null;
    }

    private static float getHealthValue(Object store, Object ref) {
        Object statMap = getComponentByName(store, ref, "EntityStatMap");
        if (statMap == null) {
            HytaleAssert.fail("Entity does not have an EntityStatMap component");
        }
        Object healthEntry = getHealthEntry(statMap);
        return invokeFloatMethod(healthEntry, "get");
    }

    private static Object getHealthEntry(Object statMap) {
        try {
            Object healthType = findHealthStatType();
            if (healthType != null) {
                for (Method method : statMap.getClass().getMethods()) {
                    if ("get".equals(method.getName()) && method.getParameterCount() == 1) {
                        Object entry = method.invoke(statMap, healthType);
                        if (entry != null) {
                            return entry;
                        }
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get health stat entry: %s", e.getMessage());
        }
        HytaleAssert.fail("Could not find health stat entry in EntityStatMap");
        return null;
    }

    private static Object findHealthStatType() {
        try {
            Class<?> statTypes = Class.forName("com.hypixel.hytale.server.ecs.component.stat.StatTypes");
            for (java.lang.reflect.Field field : statTypes.getFields()) {
                if ("HEALTH".equalsIgnoreCase(field.getName())) {
                    return field.get(null);
                }
            }
        } catch (Exception ignored) {
        }
        try {
            Class<?> statTypes = Class.forName("com.hypixel.hytale.server.stat.StatTypes");
            for (java.lang.reflect.Field field : statTypes.getFields()) {
                if ("HEALTH".equalsIgnoreCase(field.getName())) {
                    return field.get(null);
                }
            }
        } catch (Exception ignored) {
        }
        HytaleAssert.fail("Could not locate health StatType via reflection");
        return null;
    }

    private static boolean hasComponentByName(Object store, Object ref, String componentSimpleName) {
        return getComponentByName(store, ref, componentSimpleName) != null;
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

    private static float invokeFloatMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    if (result instanceof Number) {
                        return ((Number) result).floatValue();
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return 0f;
    }
}
