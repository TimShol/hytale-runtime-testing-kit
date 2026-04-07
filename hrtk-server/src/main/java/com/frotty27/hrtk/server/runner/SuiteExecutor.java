package com.frotty27.hrtk.server.runner;

import com.frotty27.hrtk.api.annotation.ValueSource;
import com.frotty27.hrtk.api.context.TestContext;
import com.frotty27.hrtk.api.lifecycle.*;
import com.frotty27.hrtk.server.context.LiveTestContext;
import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestMethodInfo;
import com.frotty27.hrtk.server.isolation.TestEntityTracker;
import com.frotty27.hrtk.server.isolation.TestWorldManager;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SuiteExecutor {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final TestExecutor testExecutor;
    private final BenchmarkRunner benchmarkRunner;
    private final TestWorldManager worldManager;
    private final List<TestRunListener> listeners;

    public SuiteExecutor(TestExecutor testExecutor, TestWorldManager worldManager,
                         List<TestRunListener> listeners) {
        this.testExecutor = testExecutor;
        this.benchmarkRunner = new BenchmarkRunner();
        this.worldManager = worldManager;
        this.listeners = listeners;
    }

    public SuiteResult execute(TestClassInfo suite, TestFilter filter) {
        String pluginName = suite.getPluginName();
        String suiteName = suite.getSuiteName();
        List<TestResult> results = new ArrayList<>();
        long suiteStart = System.currentTimeMillis();

        if (suite.isClassDisabled()) {
            for (TestMethodInfo method : suite.getTestMethods()) {
                results.add(new TestResult(pluginName, suiteName, method.getMethod().getName(),
                        method.getDisplayName(), TestStatus.SKIPPED, 0,
                        suite.getClassDisabledReason() != null ? suite.getClassDisabledReason() : "disabled",
                        null, new ArrayList<>(suite.getClassTags())));
            }
            long duration = System.currentTimeMillis() - suiteStart;
            return new SuiteResult(pluginName, suiteName, results, duration);
        }

        Object suiteInstance;
        try {
            suiteInstance = suite.getTestClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            for (TestMethodInfo method : suite.getTestMethods()) {
                results.add(new TestResult(pluginName, suiteName, method.getMethod().getName(),
                        method.getDisplayName(), TestStatus.ERRORED, 0,
                        "Failed to instantiate suite: " + e.getMessage(),
                        null, new ArrayList<>(suite.getClassTags())));
            }
            long duration = System.currentTimeMillis() - suiteStart;
            return new SuiteResult(pluginName, suiteName, results, duration);
        }

        IsolationStrategy isolation = suite.getIsolation();
        World dedicatedWorld = null;
        TestEntityTracker entityTracker = null;

        World suiteWorld = null;

        switch (isolation) {
            case DEDICATED_WORLD -> {
                dedicatedWorld = worldManager.getOrCreateTestWorld(suiteName);
                suiteWorld = dedicatedWorld;
            }
            case SNAPSHOT -> {
                suiteWorld = resolveAnyWorld();
                if (suiteWorld != null) {
                    EntityStore entityStore = suiteWorld.getEntityStore();
                    entityTracker = new TestEntityTracker(entityStore.getStore());
                }
            }
            case NONE -> {
                suiteWorld = resolveAnyWorld();
            }
        }

        testExecutor.setActiveEntityTracker(entityTracker);
        testExecutor.setActiveWorld(suiteWorld);

        TestContext baseContext = new LiveTestContext(pluginName);

        boolean beforeAllFailed = false;
        try {
            for (Method method : suite.getBeforeAllMethods()) {
                method.setAccessible(true);
                if (Modifier.isStatic(method.getModifiers())) {
                    method.invoke(null);
                } else {
                    method.invoke(suiteInstance);
                }
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("HRTK: @BeforeAll failed in %s: %s", suiteName, e.getMessage());
            for (TestMethodInfo methodInfo : suite.getTestMethods()) {
                results.add(new TestResult(pluginName, suiteName, methodInfo.getMethod().getName(),
                        methodInfo.getDisplayName(), TestStatus.ERRORED, 0,
                        "@BeforeAll failed: " + e.getMessage(), null, new ArrayList<>(suite.getClassTags())));
            }
            beforeAllFailed = true;
        }

        if (!beforeAllFailed) {
            for (TestMethodInfo methodInfo : suite.getTestMethods()) {
                if (!filter.matchesMethod(suite, methodInfo)) continue;

                boolean beforeEachFailed = false;
                try {
                    for (Method method : suite.getBeforeEachMethods()) {
                        method.setAccessible(true);
                        method.invoke(suiteInstance);
                    }
                } catch (Exception e) {
                    LOGGER.atWarning().log("HRTK: @BeforeEach failed in %s: %s", suiteName, e.getMessage());
                    results.add(new TestResult(pluginName, suiteName, methodInfo.getMethod().getName(),
                            methodInfo.getDisplayName(), TestStatus.ERRORED, 0,
                            "@BeforeEach failed: " + e.getMessage(), null, new ArrayList<>(suite.getClassTags())));
                    beforeEachFailed = true;
                }

                if (beforeEachFailed) {
                    try {
                        for (Method method : suite.getAfterEachMethods()) {
                            method.setAccessible(true);
                            method.invoke(suiteInstance);
                        }
                    } catch (Exception _) {}
                    continue;
                }

                if (methodInfo.isBenchmark()) {
                    TestResult result = benchmarkRunner.runBenchmark(suite, methodInfo, suiteInstance);
                    results.add(result);
                    for (TestRunListener listener : listeners) {
                        listener.onTestCompleted(result);
                    }
                } else if (methodInfo.isParameterized()) {
                    List<Object> paramValues = extractParameterValues(methodInfo.getMethod());
                    if (paramValues.isEmpty()) {
                        TestResult result = new TestResult(pluginName, suiteName, methodInfo.getMethod().getName(),
                                methodInfo.getDisplayName(), TestStatus.ERRORED, 0,
                                "No parameter values provided. Add @ValueSource to the test method.",
                                null, new ArrayList<>(suite.getClassTags()));
                        results.add(result);
                        for (TestRunListener listener : listeners) {
                            listener.onTestCompleted(result);
                        }
                    }
                    for (int i = 0; i < paramValues.size(); i++) {
                        TestResult result = testExecutor.executeParameterized(
                                suite, methodInfo, suiteInstance, baseContext, paramValues.get(i), i);
                        results.add(result);
                        for (TestRunListener listener : listeners) {
                            listener.onTestCompleted(result);
                        }
                    }
                } else {
                    int repetitions = methodInfo.isRepeatedTest() ? methodInfo.getRepeatCount() : 1;
                    if (repetitions <= 0) {
                        results.add(new TestResult(pluginName, suiteName, methodInfo.getMethod().getName(),
                                methodInfo.getDisplayName(), TestStatus.ERRORED, 0,
                                "Invalid repeat count: " + repetitions + ". Must be > 0.",
                                null, new ArrayList<>(suite.getClassTags())));
                    }
                    for (int r = 0; r < repetitions; r++) {
                        TestResult result = testExecutor.execute(suite, methodInfo, suiteInstance, baseContext);
                        results.add(result);
                        for (TestRunListener listener : listeners) {
                            listener.onTestCompleted(result);
                        }
                    }
                }

                try {
                    for (Method method : suite.getAfterEachMethods()) {
                        method.setAccessible(true);
                        method.invoke(suiteInstance);
                    }
                } catch (Exception e) {
                    LOGGER.atWarning().log("HRTK: @AfterEach failed in %s: %s", suiteName, e.getMessage());
                    results.add(new TestResult(pluginName, suiteName,
                            methodInfo.getMethod().getName() + "_afterEach",
                            methodInfo.getDisplayName() + " [@AfterEach]",
                            TestStatus.ERRORED, 0, "@AfterEach failed: " + e.getMessage(),
                            null, new ArrayList<>(suite.getClassTags())));
                }

                if (filter.isFailFast() && results.stream().anyMatch(TestResult::failed)) {
                    break;
                }
            }
        }

        try {
            for (Method method : suite.getAfterAllMethods()) {
                method.setAccessible(true);
                if (Modifier.isStatic(method.getModifiers())) {
                    method.invoke(null);
                } else {
                    method.invoke(suiteInstance);
                }
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("HRTK: @AfterAll failed in %s: %s", suiteName, e.getMessage());
        }

        if (baseContext instanceof LiveTestContext liveContext) {
            liveContext.cleanup();
        }

        switch (isolation) {
            case DEDICATED_WORLD -> {
                worldManager.cleanupTestWorld(suiteName);
            }
            case SNAPSHOT -> {
                if (entityTracker != null) {
                    entityTracker.restore();
                }
            }
            case NONE -> {}
        }

        testExecutor.setActiveEntityTracker(null);
        testExecutor.setActiveWorld(null);

        long duration = System.currentTimeMillis() - suiteStart;
        return new SuiteResult(pluginName, suiteName, results, duration);
    }

    private List<Object> extractParameterValues(Method method) {
        List<Object> values = new ArrayList<>();
        ValueSource valueSource = method.getAnnotation(ValueSource.class);
        if (valueSource == null) return values;

        for (int value : valueSource.ints()) values.add(value);
        for (long value : valueSource.longs()) values.add(value);
        for (double value : valueSource.doubles()) values.add(value);
        for (float value : valueSource.floats()) values.add(value);
        for (String value : valueSource.strings()) values.add(value);
        for (boolean value : valueSource.booleans()) values.add(value);

        return values;
    }

    private World resolveAnyWorld() {
        try {
            Universe universe = Universe.get();
            Map<String, World> worlds = universe.getWorlds();
            if (worlds != null && !worlds.isEmpty()) {
                return worlds.values().iterator().next();
            }
        } catch (Exception _) {
        }
        return null;
    }
}
