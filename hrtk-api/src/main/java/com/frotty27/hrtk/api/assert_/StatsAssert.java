package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for entity stat mechanics - health, stamina, modifiers, and alive/dead state.
 *
 * <p>All methods accept {@code Object} for store, ref, and stat-related types to avoid
 * coupling the API module to HytaleServer.jar. At runtime, these map to
 * {@code Store<EntityStore>}, {@code Ref<EntityStore>}, and {@code StatType}
 * respectively. Stat values and modifiers are accessed reflectively through the
 * {@code EntityStatMap} component.</p>
 *
 * <pre>{@code
 * StatsAssert.assertStatEquals(store, ref, StatTypes.HEALTH, 20.0f);
 * StatsAssert.assertStatAtMax(store, ref, StatTypes.STAMINA);
 * StatsAssert.assertAlive(store, ref);
 * StatsAssert.assertHasModifier(store, ref, StatTypes.SPEED, "sprint_boost");
 * }</pre>
 *
 * @see CombatAssert
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class StatsAssert {

    private StatsAssert() {}

    /**
     * Asserts that a stat's current value equals the expected value within the given tolerance.
     *
     * <p>Failure message: {@code "Expected stat <type> to be <expected> (tolerance <tol>) but was <actual>"}</p>
     *
     * @param store     the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref       the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType  the stat type to check (runtime type: {@code StatType})
     * @param expected  the expected stat value
     * @param tolerance the acceptable deviation from the expected value
     * @throws AssertionFailedException if the stat value differs from expected by more than the tolerance
     */
    public static void assertStatEquals(Object store, Object ref, Object statType,
                                        float expected, float tolerance) {
        float actual = getStatValue(store, ref, statType);
        if (Math.abs(expected - actual) > tolerance) {
            HytaleAssert.fail("Expected stat <%s> to be <%s> (tolerance %s) but was <%s>",
                    statType, expected, tolerance, actual);
        }
    }

    /**
     * Asserts that a stat's current value equals the expected value with a default tolerance of 0.01.
     *
     * <p>Failure message: {@code "Expected stat <type> to be <expected> (tolerance 0.01) but was <actual>"}</p>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType the stat type to check (runtime type: {@code StatType})
     * @param expected the expected stat value
     * @throws AssertionFailedException if the stat value differs from expected by more than 0.01
     */
    public static void assertStatEquals(Object store, Object ref, Object statType, float expected) {
        assertStatEquals(store, ref, statType, expected, 0.01f);
    }

    /**
     * Asserts that a stat's current value is between {@code min} and {@code max} (inclusive).
     *
     * <p>Failure message: {@code "Expected stat <type> to be between <min> and <max> but was <actual>"}</p>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType the stat type to check (runtime type: {@code StatType})
     * @param min      the minimum acceptable value (inclusive)
     * @param max      the maximum acceptable value (inclusive)
     * @throws AssertionFailedException if the stat value is outside the range
     */
    public static void assertStatBetween(Object store, Object ref, Object statType,
                                         float min, float max) {
        float actual = getStatValue(store, ref, statType);
        if (actual < min || actual > max) {
            HytaleAssert.fail("Expected stat <%s> to be between <%s> and <%s> but was <%s>",
                    statType, min, max, actual);
        }
    }

    /**
     * Asserts that a stat is at its maximum value (within 0.01 tolerance).
     *
     * <p>Compares the current value from {@code get()} against the max from {@code getMax()}
     * on the stat entry.</p>
     *
     * <p>Failure message: {@code "Expected stat <type> to be at max <max> but was <current>"}</p>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType the stat type to check (runtime type: {@code StatType})
     * @throws AssertionFailedException if the stat is not at its maximum
     */
    public static void assertStatAtMax(Object store, Object ref, Object statType) {
        Object statEntry = getStatEntry(store, ref, statType);
        float current = invokeFloatMethod(statEntry, "get");
        float max = invokeFloatMethod(statEntry, "getMax");
        if (Math.abs(current - max) > 0.01f) {
            HytaleAssert.fail("Expected stat <%s> to be at max <%s> but was <%s>",
                    statType, max, current);
        }
    }

    /**
     * Asserts that a stat is at its minimum value (within 0.01 tolerance).
     *
     * <p>Compares the current value from {@code get()} against the min from {@code getMin()}
     * on the stat entry.</p>
     *
     * <p>Failure message: {@code "Expected stat <type> to be at min <min> but was <current>"}</p>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType the stat type to check (runtime type: {@code StatType})
     * @throws AssertionFailedException if the stat is not at its minimum
     */
    public static void assertStatAtMin(Object store, Object ref, Object statType) {
        Object statEntry = getStatEntry(store, ref, statType);
        float current = invokeFloatMethod(statEntry, "get");
        float min = invokeFloatMethod(statEntry, "getMin");
        if (Math.abs(current - min) > 0.01f) {
            HytaleAssert.fail("Expected stat <%s> to be at min <%s> but was <%s>",
                    statType, min, current);
        }
    }

    /**
     * Asserts that an entity's health equals the expected value (within 0.01 tolerance).
     *
     * <p>Health is retrieved from the {@code HEALTH} stat type in the entity's
     * {@code EntityStatMap} component.</p>
     *
     * <p>Failure message: {@code "Expected health to be <expected> but was <actual>"}</p>
     *
     * @param store    the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref      the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param expected the expected health value
     * @throws AssertionFailedException if the health differs from expected by more than 0.01
     */
    public static void assertHealthEquals(Object store, Object ref, float expected) {
        float actual = getHealthValue(store, ref);
        if (Math.abs(expected - actual) > 0.01f) {
            HytaleAssert.fail("Expected health to be <%s> but was <%s>", expected, actual);
        }
    }

    /**
     * Asserts that an entity's health is at its maximum (within 0.01 tolerance).
     *
     * <p>Failure message: {@code "Expected health to be at max <max> but was <current>"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the health is not at its maximum
     */
    public static void assertHealthAtMax(Object store, Object ref) {
        Object statEntry = getHealthEntry(store, ref);
        float current = invokeFloatMethod(statEntry, "get");
        float max = invokeFloatMethod(statEntry, "getMax");
        if (Math.abs(current - max) > 0.01f) {
            HytaleAssert.fail("Expected health to be at max <%s> but was <%s>", max, current);
        }
    }

    /**
     * Asserts that an entity is alive (health greater than 0 and no {@code DeathComponent}).
     *
     * <p>Failure message: {@code "Expected entity to be alive but health was <health>"} or
     * {@code "Expected entity to be alive but it has a DeathComponent"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the entity is dead
     */
    public static void assertAlive(Object store, Object ref) {
        float health = getHealthValue(store, ref);
        if (health <= 0f) {
            HytaleAssert.fail("Expected entity to be alive but health was <%s>", health);
        }
        if (hasComponentByName(store, ref, "DeathComponent")) {
            HytaleAssert.fail("Expected entity to be alive but it has a DeathComponent");
        }
    }

    /**
     * Asserts that an entity is dead (has a {@code DeathComponent}).
     *
     * <p>Failure message: {@code "Expected entity to be dead (have DeathComponent) but it does not"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws AssertionFailedException if the entity does not have a DeathComponent
     */
    public static void assertDead(Object store, Object ref) {
        if (!hasComponentByName(store, ref, "DeathComponent")) {
            HytaleAssert.fail("Expected entity to be dead (have DeathComponent) but it does not");
        }
    }

    /**
     * Asserts that an entity's stat has a modifier with the given identifier.
     *
     * <p>Scans the stat entry's modifiers for one whose {@code getId()} or {@code getName()}
     * matches the provided identifier.</p>
     *
     * <p>Failure message: {@code "Expected stat <type> to have modifier 'id' but it was absent"}</p>
     *
     * @param store      the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref        the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType   the stat type to check (runtime type: {@code StatType})
     * @param modifierId the expected modifier identifier
     * @throws AssertionFailedException if the modifier is not found on the stat
     */
    public static void assertHasModifier(Object store, Object ref, Object statType,
                                         String modifierId) {
        Object statEntry = getStatEntry(store, ref, statType);
        if (!hasModifier(statEntry, modifierId)) {
            HytaleAssert.fail("Expected stat <%s> to have modifier '%s' but it was absent",
                    statType, modifierId);
        }
    }

    /**
     * Asserts that an entity's stat does NOT have a modifier with the given identifier.
     *
     * <p>Failure message: {@code "Expected stat <type> to NOT have modifier 'id' but it was present"}</p>
     *
     * @param store      the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref        the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType   the stat type to check (runtime type: {@code StatType})
     * @param modifierId the modifier identifier that should be absent
     * @throws AssertionFailedException if the modifier is found on the stat
     */
    public static void assertNoModifier(Object store, Object ref, Object statType,
                                        String modifierId) {
        Object statEntry = getStatEntry(store, ref, statType);
        if (hasModifier(statEntry, modifierId)) {
            HytaleAssert.fail("Expected stat <%s> to NOT have modifier '%s' but it was present",
                    statType, modifierId);
        }
    }

    /**
     * Asserts that a stat's current value, as a percentage of its max, matches the expected
     * percentage within the given tolerance.
     *
     * <p>The percentage is computed as {@code (current / max) * 100}. For example, if
     * health is 15 out of 20, the percentage is 75.0.</p>
     *
     * <p>Failure message: {@code "Expected stat <type> at <expected>% (tolerance <tol>%) but was <actual>%"}</p>
     *
     * @param store           the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref             the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param statType        the stat type to check (runtime type: {@code StatType})
     * @param expectedPercent the expected percentage (0-100)
     * @param tolerance       the acceptable deviation in percentage points
     * @throws AssertionFailedException if the stat percentage differs from expected by more than the tolerance
     */
    public static void assertStatPercentage(Object store, Object ref, Object statType,
                                            float expectedPercent, float tolerance) {
        if (store == null) {
            HytaleAssert.fail("store must not be null");
        }
        if (ref == null) {
            HytaleAssert.fail("ref must not be null");
        }
        if (statType == null) {
            HytaleAssert.fail("statType must not be null");
        }
        Object statEntry = getStatEntry(store, ref, statType);
        float current = invokeFloatMethod(statEntry, "get");
        float max = invokeFloatMethod(statEntry, "getMax");
        if (max == 0f) {
            HytaleAssert.fail("Stat <%s> has max of 0 - cannot compute percentage", statType);
        }
        float actualPercent = (current / max) * 100f;
        if (Math.abs(expectedPercent - actualPercent) > tolerance) {
            HytaleAssert.fail("Expected stat <%s> at <%s%%> (tolerance %s%%) but was <%s%%>",
                    statType, expectedPercent, tolerance, actualPercent);
        }
    }

    private static Object getStatEntry(Object store, Object ref, Object statType) {
        Object statMap = getStatMapComponent(store, ref);
        try {
            for (Method method : statMap.getClass().getMethods()) {
                if ("get".equals(method.getName()) && method.getParameterCount() == 1) {
                    Object entry = method.invoke(statMap, statType);
                    if (entry == null) {
                        HytaleAssert.fail("Stat <%s> not found in EntityStatMap", statType);
                    }
                    return entry;
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to get stat entry: %s", e.getMessage());
        }
        HytaleAssert.fail("EntityStatMap has no get(statType) method");
        return null;
    }

    private static float getStatValue(Object store, Object ref, Object statType) {
        Object statEntry = getStatEntry(store, ref, statType);
        return invokeFloatMethod(statEntry, "get");
    }

    private static float getHealthValue(Object store, Object ref) {
        Object statEntry = getHealthEntry(store, ref);
        return invokeFloatMethod(statEntry, "get");
    }

    private static Object getHealthEntry(Object store, Object ref) {
        Object statMap = getStatMapComponent(store, ref);
        try {
            for (Method method : statMap.getClass().getMethods()) {
                if ("get".equals(method.getName()) && method.getParameterCount() == 1) {
                    Object healthType = findHealthStatType();
                    if (healthType != null) {
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

    private static Object getStatMapComponent(Object store, Object ref) {
        Object component = getComponentByName(store, ref, "EntityStatMap");
        if (component == null) {
            HytaleAssert.fail("Entity does not have an EntityStatMap component");
        }
        return component;
    }

    private static boolean hasComponentByName(Object store, Object ref, String componentSimpleName) {
        return getComponentByName(store, ref, componentSimpleName) != null;
    }

    private static Object getComponentByName(Object store, Object ref, String componentSimpleName) {
        try {
            for (Method method : store.getClass().getMethods()) {
                if ("getComponent".equals(method.getName()) && method.getParameterCount() == 2) {
                    Method getTypes = null;
                    for (Method m : store.getClass().getMethods()) {
                        if ("getComponentTypes".equals(m.getName()) && m.getParameterCount() == 0) {
                            getTypes = m;
                            break;
                        }
                    }
                    if (getTypes != null) {
                        Object types = getTypes.invoke(store);
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

    private static boolean hasModifier(Object statEntry, String modifierId) {
        try {
            for (Method method : statEntry.getClass().getMethods()) {
                if ("getModifiers".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object modifiers = method.invoke(statEntry);
                    if (modifiers instanceof Iterable<?>) {
                        for (Object modifier : (Iterable<?>) modifiers) {
                            String id = invokeStringMethod(modifier, "getId");
                            if (id == null) {
                                id = invokeStringMethod(modifier, "getName");
                            }
                            if (modifierId.equals(id)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to check modifiers: %s", e.getMessage());
        }
        return false;
    }

    private static String invokeStringMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(target);
                    return result != null ? result.toString() : null;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
