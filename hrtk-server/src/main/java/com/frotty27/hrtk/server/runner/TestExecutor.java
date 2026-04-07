package com.frotty27.hrtk.server.runner;

import com.frotty27.hrtk.api.annotation.RequiresPlugin;
import com.frotty27.hrtk.api.assert_.AssertionFailedException;
import com.frotty27.hrtk.api.context.BenchmarkContext;
import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.api.context.TestContext;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;
import com.frotty27.hrtk.server.context.LiveEcsTestContext;
import com.frotty27.hrtk.server.context.LiveTestContext;
import com.frotty27.hrtk.server.context.LiveWorldTestContext;
import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestMethodInfo;
import com.frotty27.hrtk.server.isolation.TestEntityTracker;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class TestExecutor {

    private volatile TestEntityTracker activeEntityTracker;
    private volatile World activeWorld;

    public void setActiveEntityTracker(TestEntityTracker entityTracker) {
        this.activeEntityTracker = entityTracker;
    }

    public void setActiveWorld(World world) {
        this.activeWorld = world;
    }

    public TestResult execute(TestClassInfo suite, TestMethodInfo methodInfo,
                              Object suiteInstance, TestContext baseContext) {
        String pluginName = suite.getPluginName();
        String suiteName = suite.getSuiteName();
        String testName = methodInfo.getMethod().getName();
        String displayName = methodInfo.getDisplayName();
        LinkedHashSet<String> tagSet = new LinkedHashSet<>(suite.getClassTags());
        tagSet.addAll(methodInfo.getTags());
        List<String> allTags = new ArrayList<>(tagSet);

        if (methodInfo.isDisabled()) {
            String reason = methodInfo.getDisabledReason();
            return new TestResult(pluginName, suiteName, testName, displayName,
                    TestStatus.SKIPPED, 0, reason != null && !reason.isEmpty() ? reason : "disabled",
                    null, allTags);
        }

        RequiresPlugin requiresPlugin = methodInfo.getMethod().getAnnotation(RequiresPlugin.class);
        if (requiresPlugin != null) {
            String requiredName = requiresPlugin.value();
            boolean found = false;
            for (PluginBase plugin : PluginManager.get().getPlugins()) {
                String name = plugin.getName();
                if (requiredName.equalsIgnoreCase(name) || name.endsWith(":" + requiredName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return new TestResult(pluginName, suiteName, testName, displayName,
                        TestStatus.SKIPPED, 0, "Required plugin '" + requiredName + "' not loaded",
                        null, allTags);
            }
        }

        TestContext context = createContext(methodInfo, pluginName);
        long startTime = System.currentTimeMillis();

        try {
            Object[] params = injectParameters(methodInfo.getMethod(), context);
            long timeoutMs = methodInfo.getTimeoutMs();

            if (methodInfo.requiresWorld() && !(context instanceof WorldTestContext) && !(context instanceof EcsTestContext)) {
                return new TestResult(pluginName, suiteName, testName, displayName,
                        TestStatus.ERRORED, 0, "No world available for world-requiring test",
                        null, allTags);
            }

            World world = resolveWorldFromContext(context);
            CompletableFuture<Void> future = new CompletableFuture<>();

            if (world != null && !methodInfo.needsTickWaiting()) {
                world.execute(() -> {
                    try {
                        methodInfo.getMethod().invoke(suiteInstance, params);
                        future.complete(null);
                    } catch (Throwable t) {
                        future.completeExceptionally(t.getCause() != null ? t.getCause() : t);
                    }
                });
            } else {
                CompletableFuture.runAsync(() -> {
                    try {
                        methodInfo.getMethod().invoke(suiteInstance, params);
                    } catch (Throwable t) {
                        Throwable cause = t.getCause() != null ? t.getCause() : t;
                        throw new RuntimeException(cause);
                    }
                }).whenComplete((v, ex) -> {
                    if (ex != null) future.completeExceptionally(ex);
                    else future.complete(null);
                });
            }

            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException _) {
                long duration = System.currentTimeMillis() - startTime;
                return new TestResult(pluginName, suiteName, testName, displayName,
                        TestStatus.TIMED_OUT, duration, "Timed out after " + timeoutMs + "ms",
                        null, allTags);
            }

            long duration = System.currentTimeMillis() - startTime;

            return new TestResult(pluginName, suiteName, testName, displayName,
                    TestStatus.PASSED, duration, null, null, allTags);

        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            Throwable cause = unwrapCause(t);

            if (cause instanceof AssertionFailedException) {
                return new TestResult(pluginName, suiteName, testName, displayName,
                        TestStatus.FAILED, duration, cause.getMessage(),
                        formatStackTrace(cause), allTags);
            } else {
                return new TestResult(pluginName, suiteName, testName, displayName,
                        TestStatus.ERRORED, duration,
                        cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                        formatStackTrace(cause), allTags);
            }
        } finally {
            if (context instanceof LiveTestContext liveContext) {
                liveContext.cleanup();
            }
        }
    }

    public TestResult executeParameterized(TestClassInfo suite, TestMethodInfo methodInfo,
                                            Object suiteInstance, TestContext baseContext,
                                            Object paramValue, int paramIndex) {
        String displayName = methodInfo.getDisplayName() + " [" + paramIndex + "] " + paramValue;
        String testName = methodInfo.getMethod().getName() + "[" + paramIndex + "]";

        LinkedHashSet<String> tagSet = new LinkedHashSet<>(suite.getClassTags());
        tagSet.addAll(methodInfo.getTags());
        List<String> allTags = new ArrayList<>(tagSet);

        if (methodInfo.isDisabled()) {
            return new TestResult(suite.getPluginName(), suite.getSuiteName(), testName, displayName,
                    TestStatus.SKIPPED, 0, "disabled", null, allTags);
        }

        TestContext context = createContext(methodInfo, suite.getPluginName());
        long startTime = System.currentTimeMillis();

        try {
            Method method = methodInfo.getMethod();
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            if (paramTypes.length > 0) {
                params[0] = paramValue;
            }
            for (int i = 1; i < paramTypes.length; i++) {
                params[i] = resolveParameter(paramTypes[i], context);
            }

            long timeoutMs = methodInfo.getTimeoutMs();
            World world = resolveWorldFromContext(context);
            CompletableFuture<Void> future = new CompletableFuture<>();

            if (world != null && !methodInfo.needsTickWaiting()) {
                world.execute(() -> {
                    try {
                        method.invoke(suiteInstance, params);
                        future.complete(null);
                    } catch (Throwable t) {
                        future.completeExceptionally(t.getCause() != null ? t.getCause() : t);
                    }
                });
            } else {
                CompletableFuture.runAsync(() -> {
                    try {
                        method.invoke(suiteInstance, params);
                    } catch (Throwable t) {
                        Throwable cause = t.getCause() != null ? t.getCause() : t;
                        throw new RuntimeException(cause);
                    }
                }).whenComplete((v, ex) -> {
                    if (ex != null) future.completeExceptionally(ex);
                    else future.complete(null);
                });
            }

            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException _) {
                long duration = System.currentTimeMillis() - startTime;
                return new TestResult(suite.getPluginName(), suite.getSuiteName(), testName, displayName,
                        TestStatus.TIMED_OUT, duration, "Timed out after " + timeoutMs + "ms",
                        null, allTags);
            }

            long duration = System.currentTimeMillis() - startTime;
            return new TestResult(suite.getPluginName(), suite.getSuiteName(), testName, displayName,
                    TestStatus.PASSED, duration, null, null, allTags);

        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            Throwable cause = unwrapCause(t);
            TestStatus status = cause instanceof AssertionFailedException
                    ? TestStatus.FAILED : TestStatus.ERRORED;
            return new TestResult(suite.getPluginName(), suite.getSuiteName(), testName, displayName,
                    status, duration, cause.getMessage(), formatStackTrace(cause), allTags);
        }
    }

    private TestContext createContext(TestMethodInfo methodInfo, String pluginName) {
        if (methodInfo.isEcsTest() || methodInfo.requiresWorld()) {
            World world = resolveWorld(methodInfo.getWorldName());
            if (world != null) {
                if (methodInfo.isEcsTest()) {
                    EntityStore entityStore = world.getEntityStore();
                    Store<EntityStore> store = entityStore.getStore();
                    CommandBuffer<EntityStore> commandBuffer = createCommandBuffer(store);
                    LiveEcsTestContext ecsContext = new LiveEcsTestContext(pluginName, store, commandBuffer);
                    if (activeEntityTracker != null) ecsContext.setEntityTracker(activeEntityTracker);
                    return ecsContext;
                } else {
                    LiveWorldTestContext worldContext = new LiveWorldTestContext(pluginName, world);
                    if (activeEntityTracker != null) worldContext.setEntityTracker(activeEntityTracker);
                    return worldContext;
                }
            }
        }
        return new LiveTestContext(pluginName);
    }

    private World resolveWorld(String worldName) {
        try {
            if (worldName != null && !worldName.isEmpty()) {
                return Universe.get().getWorld(worldName);
            }
            if (activeWorld != null && activeWorld.isAlive()) {
                return activeWorld;
            }
            Map<String, World> worlds = Universe.get().getWorlds();
            if (worlds != null && !worlds.isEmpty()) {
                return worlds.values().iterator().next();
            }
        } catch (Exception _) {
        }
        return null;
    }

    private World resolveWorldFromContext(TestContext context) {
        if (context instanceof LiveWorldTestContext wc) return wc.getWorld();
        if (context instanceof LiveEcsTestContext ec) return ec.getWorld();
        return null;
    }

    private Object[] injectParameters(Method method, TestContext context) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = resolveParameter(paramTypes[i], context);
        }
        return params;
    }

    private Object resolveParameter(Class<?> type, TestContext context) {
        if (type.isAssignableFrom(context.getClass())) {
            return context;
        }

        String simpleName = type.getSimpleName();
        return switch (simpleName) {
            case "TestContext" -> context;
            case "WorldTestContext" -> context instanceof WorldTestContext ? context : null;
            case "EcsTestContext" -> context instanceof EcsTestContext ? context : null;
            case "BenchmarkContext" -> context instanceof BenchmarkContext ? context : null;
            case "MockCommandSender" -> context.createCommandSender();
            case "World" -> context instanceof WorldTestContext worldContext ? worldContext.getWorld() : null;
            case "Store" -> {
                if (context instanceof WorldTestContext worldContext) yield worldContext.getStore();
                else if (context instanceof EcsTestContext ecsContext) yield ecsContext.getStore();
                else yield null;
            }
            case "CommandBuffer" -> {
                if (context instanceof WorldTestContext worldContext) yield worldContext.getCommandBuffer();
                else if (context instanceof EcsTestContext ecsContext) yield ecsContext.getCommandBuffer();
                else yield null;
            }
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private CommandBuffer<EntityStore> createCommandBuffer(Store<EntityStore> store) {
        try {
            var ctor = CommandBuffer.class.getDeclaredConstructor(Store.class);
            ctor.setAccessible(true);
            return (CommandBuffer<EntityStore>) ctor.newInstance(store);
        } catch (Exception e) {
            return null;
        }
    }

    private Throwable unwrapCause(Throwable t) {
        while (t.getCause() != null && t != t.getCause()) {
            t = t.getCause();
        }
        return t;
    }

    private String formatStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
