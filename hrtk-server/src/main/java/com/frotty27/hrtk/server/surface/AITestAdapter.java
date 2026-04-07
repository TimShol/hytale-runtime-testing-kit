package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;

public final class AITestAdapter {

    private static final String STATE_EVALUATOR = "com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator";

    private AITestAdapter() {}

    public static Object getStateEvaluator(Object store, Object ref) {
        try {
            if (store == null || ref == null) return null;
            Class<?> evaluatorClass = Class.forName(STATE_EVALUATOR);
            Method getComponentType = evaluatorClass.getMethod("getComponentType");
            Object componentType = getComponentType.invoke(null);
            Method getComponent = store.getClass().getMethod("getComponent", ref.getClass(), componentType.getClass());
            return getComponent.invoke(store, ref, componentType);
        } catch (NoSuchMethodException _) {
            return getStateEvaluatorFallback(store, ref);
        } catch (Exception _) {
            return null;
        }
    }

    private static Object getStateEvaluatorFallback(Object store, Object ref) {
        try {
            Class<?> evaluatorClass = Class.forName(STATE_EVALUATOR);
            Method getComponentType = evaluatorClass.getMethod("getComponentType");
            Object componentType = getComponentType.invoke(null);
            for (Method m : store.getClass().getMethods()) {
                if ("getComponent".equals(m.getName()) && m.getParameterCount() == 2) {
                    try {
                        Object result = m.invoke(store, ref, componentType);
                        if (result != null) return result;
                    } catch (Exception _) {}
                }
            }
            return null;
        } catch (Exception _) {
            return null;
        }
    }

    public static boolean isAIActive(Object store, Object ref) {
        try {
            Object evaluator = getStateEvaluator(store, ref);
            if (evaluator == null) return false;
            for (Method m : evaluator.getClass().getMethods()) {
                if ("isActive".equals(m.getName()) && m.getParameterCount() == 0) {
                    Object result = m.invoke(evaluator);
                    return Boolean.TRUE.equals(result);
                }
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }

    public static boolean stateEvaluatorClassAvailable() {
        try {
            Class.forName(STATE_EVALUATOR);
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}
