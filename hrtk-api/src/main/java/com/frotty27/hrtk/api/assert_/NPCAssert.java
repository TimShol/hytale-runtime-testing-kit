package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for NPC entities - role names, despawn state, leash points, and NPC components.
 *
 * <p>All methods accept {@code Object} for store and ref to avoid coupling the
 * API module to HytaleServer.jar. At runtime, these map to
 * {@code Store<EntityStore>} and {@code Ref<EntityStore>}. NPC data is accessed
 * reflectively through {@code com.hypixel.hytale.server.npc.NPCEntity} and
 * {@code com.hypixel.hytale.server.npc.NPCPlugin}.</p>
 *
 * <pre>{@code
 * NPCAssert.assertNPCEntity(store, entityRef);
 * NPCAssert.assertRoleName(store, entityRef, "merchant");
 * NPCAssert.assertNotDespawning(store, entityRef);
 * NPCAssert.assertRoleExists("guard");
 * NPCAssert.assertLeashPoint(store, entityRef);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class NPCAssert {

    private NPCAssert() {}

    /**
     * Asserts that the NPC entity's role name matches the expected value.
     *
     * <p>Retrieves the {@code NPCEntity} component from the entity and calls
     * {@code getRoleName()} via reflection.</p>
     *
     * <p>Failure message: {@code "Expected NPC role name <expected> but was <actual>"}</p>
     *
     * <pre>{@code
     * NPCAssert.assertRoleName(store, entityRef, "merchant");
     * }</pre>
     *
     * @param store        the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref          the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param expectedRole the expected role name
     * @throws IllegalArgumentException if store, ref, or expectedRole is null
     * @throws AssertionFailedException if the role name does not match
     */
    public static void assertRoleName(Object store, Object ref, String expectedRole) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        if (expectedRole == null) {
            throw new IllegalArgumentException("expectedRole must not be null");
        }
        Object npcEntity = getNPCEntityComponent(store, ref);
        String actualRole = invokeStringMethod(npcEntity, "getRoleName");
        if (!expectedRole.equals(actualRole)) {
            HytaleAssert.fail("Expected NPC role name <%s> but was <%s>", expectedRole, actualRole);
        }
    }

    /**
     * Asserts that the NPC entity is not currently despawning.
     *
     * <p>Retrieves the {@code NPCEntity} component and calls {@code isDespawning()}
     * via reflection, asserting it returns {@code false}.</p>
     *
     * <p>Failure message: {@code "Expected NPC entity to not be despawning but isDespawning() returned true"}</p>
     *
     * <pre>{@code
     * NPCAssert.assertNotDespawning(store, entityRef);
     * }</pre>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if the NPC is despawning
     */
    public static void assertNotDespawning(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        Object npcEntity = getNPCEntityComponent(store, ref);
        boolean despawning = invokeBooleanMethod(npcEntity, "isDespawning");
        if (despawning) {
            HytaleAssert.fail("Expected NPC entity to not be despawning but isDespawning() returned true");
        }
    }

    /**
     * Asserts that a role with the given name exists in the NPC plugin registry.
     *
     * <p>Accesses {@code NPCPlugin.get()} via reflection, then calls
     * {@code hasRoleName(String)} to verify the role is registered.</p>
     *
     * <p>Failure message: {@code "Expected NPC role '<roleName>' to exist but it was not found"}</p>
     *
     * <pre>{@code
     * NPCAssert.assertRoleExists("guard");
     * }</pre>
     *
     * @param roleName the role name to check
     * @throws IllegalArgumentException if roleName is null
     * @throws AssertionFailedException if the role does not exist
     */
    public static void assertRoleExists(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("roleName must not be null");
        }
        Object plugin = getNPCPlugin();
        boolean exists = invokeHasRoleName(plugin, roleName);
        if (!exists) {
            HytaleAssert.fail("Expected NPC role '%s' to exist but it was not found", roleName);
        }
    }

    /**
     * Asserts that the entity has an {@code NPCEntity} component attached.
     *
     * <p>Failure message: {@code "Expected entity to have NPCEntity component but it was absent"}</p>
     *
     * <pre>{@code
     * NPCAssert.assertNPCEntity(store, entityRef);
     * }</pre>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if the entity does not have an NPCEntity component
     */
    public static void assertNPCEntity(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        Object component = getComponentByName(store, ref, "NPCEntity");
        if (component == null) {
            HytaleAssert.fail("Expected entity to have NPCEntity component but it was absent");
        }
    }

    /**
     * Asserts that the NPC entity has a leash point set (non-null).
     *
     * <p>Retrieves the {@code NPCEntity} component and calls {@code getLeashPoint()}
     * via reflection, asserting the result is not null.</p>
     *
     * <p>Failure message: {@code "Expected NPC entity to have a leash point but it was null"}</p>
     *
     * <pre>{@code
     * NPCAssert.assertLeashPoint(store, entityRef);
     * }</pre>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException if store or ref is null
     * @throws AssertionFailedException if the leash point is null
     */
    public static void assertLeashPoint(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        Object npcEntity = getNPCEntityComponent(store, ref);
        Object leashPoint = invokeObjectMethod(npcEntity, "getLeashPoint");
        if (leashPoint == null) {
            HytaleAssert.fail("Expected NPC entity to have a leash point but it was null");
        }
    }

    private static Object getNPCEntityComponent(Object store, Object ref) {
        Object component = getComponentByName(store, ref, "NPCEntity");
        if (component == null) {
            HytaleAssert.fail("Entity does not have an NPCEntity component");
        }
        return component;
    }

    private static Object getNPCPlugin() {
        try {
            Class<?> pluginClass = Class.forName("com.hypixel.hytale.server.npc.NPCPlugin");
            for (Method method : pluginClass.getMethods()) {
                if ("get".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object plugin = method.invoke(null);
                    if (plugin == null) {
                        HytaleAssert.fail("NPCPlugin.get() returned null");
                    }
                    return plugin;
                }
            }
        } catch (ClassNotFoundException e) {
            HytaleAssert.fail("NPC module is not available - class com.hypixel.hytale.server.npc.NPCPlugin not found");
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to access NPCPlugin.get(): %s", e.getMessage());
        }
        HytaleAssert.fail("NPCPlugin does not have a get() method");
        return null;
    }

    private static boolean invokeHasRoleName(Object plugin, String roleName) {
        try {
            for (Method method : plugin.getClass().getMethods()) {
                if ("hasRoleName".equals(method.getName()) && method.getParameterCount() == 1) {
                    Object result = method.invoke(plugin, roleName);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke NPCPlugin.hasRoleName('%s'): %s", roleName, e.getMessage());
        }
        HytaleAssert.fail("NPCPlugin does not have hasRoleName(String) method");
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
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
        return null;
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

    private static Object invokeObjectMethod(Object target, String methodName) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    return method.invoke(target);
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke %s.%s(): %s",
                    target.getClass().getSimpleName(), methodName, e.getMessage());
        }
        HytaleAssert.fail("Method %s() not found on %s", methodName, target.getClass().getSimpleName());
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
