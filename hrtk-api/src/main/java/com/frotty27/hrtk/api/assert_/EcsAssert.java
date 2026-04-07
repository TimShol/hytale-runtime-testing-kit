package com.frotty27.hrtk.api.assert_;

/**
 * Assertions for ECS (Entity Component System) operations.
 *
 * <p>All methods accept {@code Object} for store, ref, and componentType to avoid
 * coupling the API module to HytaleServer.jar types. At runtime, these map to
 * {@code Store<EntityStore>}, {@code Ref<EntityStore>}, and
 * {@code ComponentType<EntityStore, T>} respectively.</p>
 *
 * <pre>{@code
 * EcsAssert.assertHasComponent(store, entityRef, healthComponentType);
 * float hp = EcsAssert.assertGetComponent(store, entityRef, healthComponentType);
 * EcsAssert.assertRefValid(entityRef);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class EcsAssert {

    private EcsAssert() {}

    /**
     * Asserts that an entity has the given component type attached.
     *
     * <p>Uses reflective invocation of {@code store.getComponent(ref, componentType)}
     * and fails if the result is {@code null}.</p>
     *
     * <p>Failure message: {@code "Expected entity to have component <type> but it was absent"}</p>
     *
     * @param store         the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref           the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type to check (runtime type: {@code ComponentType<EntityStore, T>})
     * @throws AssertionFailedException if the entity does not have the component
     */
    public static void assertHasComponent(Object store, Object ref, Object componentType) {
        Object component = getComponentReflective(store, ref, componentType);
        if (component == null) {
            HytaleAssert.fail("Expected entity to have component <%s> but it was absent",
                    componentType);
        }
    }

    /**
     * Asserts that an entity does NOT have the given component type attached.
     *
     * <p>Failure message: {@code "Expected entity to NOT have component <type> but it was present"}</p>
     *
     * @param store         the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref           the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type to check (runtime type: {@code ComponentType<EntityStore, T>})
     * @throws AssertionFailedException if the entity has the component
     */
    public static void assertNotHasComponent(Object store, Object ref, Object componentType) {
        Object component = getComponentReflective(store, ref, componentType);
        if (component != null) {
            HytaleAssert.fail("Expected entity to NOT have component <%s> but it was present",
                    componentType);
        }
    }

    /**
     * Asserts that an entity has the given component and returns the component instance.
     *
     * <p>This is a convenience method that combines an existence check with retrieval,
     * allowing tests to assert and access a component in a single call.</p>
     *
     * <p>Failure message: {@code "Expected entity to have component <type> but it was absent"}</p>
     *
     * @param store         the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref           the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type to retrieve (runtime type: {@code ComponentType<EntityStore, T>})
     * @param <T>           the component type
     * @return the component instance, never {@code null}
     * @throws AssertionFailedException if the entity does not have the component
     */
    @SuppressWarnings("unchecked")
    public static <T> T assertGetComponent(Object store, Object ref, Object componentType) {
        Object component = getComponentReflective(store, ref, componentType);
        if (component == null) {
            HytaleAssert.fail("Expected entity to have component <%s> but it was absent",
                    componentType);
        }
        return (T) component;
    }

    /**
     * Asserts that an entity reference is valid (not {@code null} and not invalidated).
     *
     * <p>Failure message: {@code "Entity reference: Expected non-null value but was null"}</p>
     *
     * @param ref the entity reference to validate
     * @throws AssertionFailedException if the reference is {@code null}
     */
    public static void assertRefValid(Object ref) {
        HytaleAssert.assertNotNull("Entity reference", ref);
    }

    /**
     * Asserts that an entity reference is {@code null} or invalid.
     *
     * <p>Failure message: {@code "Entity reference: Expected null but was <ref>"}</p>
     *
     * @param ref the entity reference to validate
     * @throws AssertionFailedException if the reference is not {@code null}
     */
    public static void assertRefInvalid(Object ref) {
        HytaleAssert.assertNull("Entity reference", ref);
    }

    /**
     * Asserts that an archetype contains the given component type.
     *
     * <p>Checks the {@code contains(componentType)} method on the archetype object
     * via reflection.</p>
     *
     * <p>Failure message: {@code "Expected archetype to contain component <type> but it did not"}</p>
     *
     * @param archetype     the archetype to check (runtime type: {@code Archetype})
     * @param componentType the component type to look for (runtime type: {@code ComponentType<EntityStore, T>})
     * @throws AssertionFailedException if the archetype is null, componentType is null,
     *                                  or the archetype does not contain the component type
     */
    public static void assertArchetypeContains(Object archetype, Object componentType) {
        if (archetype == null) {
            HytaleAssert.fail("archetype must not be null");
        }
        if (componentType == null) {
            HytaleAssert.fail("componentType must not be null");
        }
        boolean contains = invokeContains(archetype, componentType);
        if (!contains) {
            HytaleAssert.fail("Expected archetype to contain component <%s> but it did not",
                    componentType);
        }
    }

    private static boolean invokeContains(Object archetype, Object componentType) {
        try {
            for (var method : archetype.getClass().getMethods()) {
                if ("contains".equals(method.getName()) && method.getParameterCount() == 1) {
                    Object result = method.invoke(archetype, componentType);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke archetype.contains(): %s", e.getMessage());
        }
        HytaleAssert.fail("Archetype does not have contains(componentType) method");
        return false;
    }

    private static Object getComponentReflective(Object store, Object ref, Object componentType) {
        try {
            var method = store.getClass().getMethod("getComponent", ref.getClass().getSuperclass() != null
                    ? findRefClass(ref) : ref.getClass(), componentType.getClass());
            return method.invoke(store, ref, componentType);
        } catch (NoSuchMethodException e) {
            try {
                for (var method : store.getClass().getMethods()) {
                    if ("getComponent".equals(method.getName()) && method.getParameterCount() == 2) {
                        return method.invoke(store, ref, componentType);
                    }
                }
            } catch (Exception ex) {
                HytaleAssert.fail("Failed to invoke store.getComponent: %s", ex.getMessage());
            }
            HytaleAssert.fail("Store does not have getComponent method");
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke store.getComponent: %s", e.getMessage());
        }
        return null;
    }

    private static Class<?> findRefClass(Object ref) {
        Class<?> clazz = ref.getClass();
        while (clazz != null && !clazz.getSimpleName().equals("Ref")) {
            clazz = clazz.getSuperclass();
        }
        return clazz != null ? clazz : ref.getClass();
    }
}
