package com.frotty27.hrtk.api.assert_;

import java.lang.reflect.Method;

/**
 * Assertions for AI state - checking whether entity AI is active or inactive.
 *
 * <p>Uses reflection to access {@code StateEvaluator.isActive()} without importing
 * HytaleServer.jar classes directly.</p>
 *
 * <pre>{@code
 * AIAssert.assertAIActive(store, entityRef);
 * AIAssert.assertAIInactive(store, entityRef);
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class AIAssert {

    private AIAssert() {}

    /**
     * Asserts that the entity's AI is currently active.
     *
     * <p>Uses reflection to locate the StateEvaluator component and invoke
     * {@code isActive()} on it.</p>
     *
     * <p>Failure message: {@code "Expected entity AI to be active but it was inactive"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException    if store or ref is null
     * @throws AssertionFailedException    if the entity's AI is not active
     */
    public static void assertAIActive(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        boolean active = isAIActive(store, ref);
        if (!active) {
            HytaleAssert.fail("Expected entity AI to be active but it was inactive");
        }
    }

    /**
     * Asserts that the entity's AI is currently inactive.
     *
     * <p>Uses reflection to locate the StateEvaluator component and invoke
     * {@code isActive()} on it.</p>
     *
     * <p>Failure message: {@code "Expected entity AI to be inactive but it was active"}</p>
     *
     * @param store the ECS store (runtime type: {@code Store<EntityStore>})
     * @param ref   the entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws IllegalArgumentException    if store or ref is null
     * @throws AssertionFailedException    if the entity's AI is active
     */
    public static void assertAIInactive(Object store, Object ref) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref must not be null");
        }
        boolean active = isAIActive(store, ref);
        if (active) {
            HytaleAssert.fail("Expected entity AI to be inactive but it was active");
        }
    }

    private static boolean isAIActive(Object store, Object ref) {
        Object evaluator = getComponentByName(store, ref, "StateEvaluator");
        if (evaluator == null) {
            HytaleAssert.fail("Entity does not have a StateEvaluator component");
        }
        try {
            for (Method method : evaluator.getClass().getMethods()) {
                if ("isActive".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(evaluator);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (AssertionFailedException e) {
            throw e;
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke StateEvaluator.isActive(): %s", e.getMessage());
        }
        HytaleAssert.fail("Could not determine AI active state - no isActive() method found on StateEvaluator");
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
