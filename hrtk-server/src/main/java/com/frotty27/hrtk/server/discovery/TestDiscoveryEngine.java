package com.frotty27.hrtk.server.discovery;

import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class TestDiscoveryEngine {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final PluginTestRegistry registry = new PluginTestRegistry();

    public PluginTestRegistry getRegistry() {
        return registry;
    }

    public void scanAll(PluginBase excludePlugin) {
        registry.clear();
        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if (plugin instanceof JavaPlugin javaPlugin && plugin != excludePlugin) {
                scanPlugin(javaPlugin);
            }
        }
    }

    public void scanPlugin(JavaPlugin plugin) {
        String pluginName = plugin.getName();
        registry.clearPlugin(pluginName);

        Path jarPath = plugin.getFile();
        if (jarPath == null) return;

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class")) continue;
                if (name.matches(".*\\$\\d+.*")) continue;

                String className = name.replace('/', '.').replace(".class", "");
                try {
                    Class<?> clazz = plugin.getClassLoader().loadClass(className);
                    TestClassInfo info = analyzeClass(clazz, pluginName);
                    if (info != null) {
                        registry.register(pluginName, info);
                    }
                } catch (ClassNotFoundException | LinkageError _) {
                }
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("HRTK: Failed to scan plugin JAR '%s': %s", jarPath, e.getMessage());
        }

        List<TestClassInfo> found = registry.getTestClasses(pluginName);
        if (!found.isEmpty()) {
            int testCount = found.stream().mapToInt(TestClassInfo::getTestCount).sum();
            LOGGER.atInfo().log("HRTK: Found %d test(s) in %d suite(s) from plugin '%s'",
                    testCount, found.size(), pluginName);
        }
    }

    private TestClassInfo analyzeClass(Class<?> clazz, String pluginName) {
        HytaleSuite suiteAnnotation = clazz.getAnnotation(HytaleSuite.class);

        List<TestMethodInfo> testMethods = new ArrayList<>();
        List<Method> beforeAllMethods = new ArrayList<>();
        List<Method> afterAllMethods = new ArrayList<>();
        List<Method> beforeEachMethods = new ArrayList<>();
        List<Method> afterEachMethods = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BeforeAll.class)) { method.setAccessible(true); beforeAllMethods.add(method); }
            if (method.isAnnotationPresent(AfterAll.class)) { method.setAccessible(true); afterAllMethods.add(method); }
            if (method.isAnnotationPresent(BeforeEach.class)) { method.setAccessible(true); beforeEachMethods.add(method); }
            if (method.isAnnotationPresent(AfterEach.class)) { method.setAccessible(true); afterEachMethods.add(method); }

            TestMethodInfo methodInfo = analyzeMethod(method);
            if (methodInfo != null) testMethods.add(methodInfo);
        }

        if (testMethods.isEmpty()) return null;

        testMethods.sort(Comparator.comparingInt(TestMethodInfo::getOrder));

        String suiteName = suiteAnnotation != null && !suiteAnnotation.value().isEmpty()
                ? suiteAnnotation.value() : clazz.getSimpleName();
        IsolationStrategy isolation = suiteAnnotation != null
                ? suiteAnnotation.isolation() : IsolationStrategy.NONE;

        List<String> classTags = new ArrayList<>();
        Tag classTag = clazz.getAnnotation(Tag.class);
        if (classTag != null) classTags.addAll(Arrays.asList(classTag.value()));

        Disabled classDisabled = clazz.getAnnotation(Disabled.class);

        return new TestClassInfo(clazz, suiteName, pluginName, isolation, classTags,
                classDisabled != null, classDisabled != null ? classDisabled.value() : null,
                testMethods, beforeAllMethods, afterAllMethods, beforeEachMethods, afterEachMethods);
    }

    private TestMethodInfo analyzeMethod(Method method) {
        HytaleTest hytaleTest = method.getAnnotation(HytaleTest.class);
        EcsTest ecsTest = method.getAnnotation(EcsTest.class);
        WorldTest worldTest = method.getAnnotation(WorldTest.class);
        CombatTest combatTest = method.getAnnotation(CombatTest.class);
        FlowTest flowTest = method.getAnnotation(FlowTest.class);
        InventoryTest inventoryTest = method.getAnnotation(InventoryTest.class);
        SpawnTest spawnTest = method.getAnnotation(SpawnTest.class);
        StatsTest statsTest = method.getAnnotation(StatsTest.class);
        AsyncTest asyncTest = method.getAnnotation(AsyncTest.class);
        Benchmark benchmark = method.getAnnotation(Benchmark.class);
        RepeatedTest repeatedTest = method.getAnnotation(RepeatedTest.class);
        ParameterizedTest parameterizedTest = method.getAnnotation(ParameterizedTest.class);

        boolean isTest = hytaleTest != null || ecsTest != null || worldTest != null
                || combatTest != null || flowTest != null || inventoryTest != null
                || spawnTest != null || statsTest != null || asyncTest != null
                || benchmark != null || repeatedTest != null || parameterizedTest != null;
        if (!isTest) return null;

        method.setAccessible(true);

        String displayName = method.getName();
        if (hytaleTest != null && !hytaleTest.value().isEmpty()) {
            displayName = hytaleTest.value();
        }
        DisplayName displayNameAnnotation = method.getAnnotation(DisplayName.class);
        if (displayNameAnnotation != null) {
            displayName = displayNameAnnotation.value();
        }

        List<String> tags = new ArrayList<>();
        Tag methodTag = method.getAnnotation(Tag.class);
        if (methodTag != null) {
            tags.addAll(Arrays.asList(methodTag.value()));
        }
        if (ecsTest != null && !tags.contains("ecs")) tags.add("ecs");
        if (worldTest != null && !tags.contains("integration")) tags.add("integration");
        if (combatTest != null && !tags.contains("combat")) tags.add("combat");
        if (flowTest != null && !tags.contains("flow")) tags.add("flow");
        if (inventoryTest != null && !tags.contains("inventory")) tags.add("inventory");
        if (spawnTest != null && !tags.contains("spawn")) tags.add("spawn");
        if (statsTest != null && !tags.contains("stats")) tags.add("stats");
        if (asyncTest != null && !tags.contains("async")) tags.add("async");

        Order order = method.getAnnotation(Order.class);
        int orderValue = order != null ? order.value() : Integer.MAX_VALUE;

        Disabled disabled = method.getAnnotation(Disabled.class);

        RequiresWorld requiresWorldAnnotation = method.getAnnotation(RequiresWorld.class);
        boolean requiresWorld = requiresWorldAnnotation != null || worldTest != null || ecsTest != null
                || combatTest != null || flowTest != null || inventoryTest != null || spawnTest != null;
        String worldName = "";
        if (requiresWorldAnnotation != null) worldName = requiresWorldAnnotation.value();
        if (worldTest != null && !worldTest.world().isEmpty()) worldName = worldTest.world();
        if (combatTest != null && !combatTest.world().isEmpty()) worldName = combatTest.world();
        if (flowTest != null && !flowTest.world().isEmpty()) worldName = flowTest.world();
        if (inventoryTest != null && !inventoryTest.world().isEmpty()) worldName = inventoryTest.world();
        if (spawnTest != null && !spawnTest.world().isEmpty()) worldName = spawnTest.world();

        RequiresPlayer requiresPlayerAnnotation = method.getAnnotation(RequiresPlayer.class);
        boolean requiresPlayer = requiresPlayerAnnotation != null;
        int playerCount = requiresPlayerAnnotation != null ? requiresPlayerAnnotation.count() : 0;

        Timeout timeout = method.getAnnotation(Timeout.class);
        long timeoutMs;
        if (timeout != null) {
            timeoutMs = timeout.unit().toMillis(timeout.value());
        } else if (flowTest != null) {
            timeoutMs = flowTest.timeoutTicks() * 50L;
        } else if (asyncTest != null) {
            timeoutMs = asyncTest.timeoutTicks() * 50L;
        } else {
            timeoutMs = 30_000L;
        }

        int warmup = benchmark != null ? benchmark.warmup() : 0;
        int iterations = benchmark != null ? benchmark.iterations() : 0;
        int batchSize = benchmark != null ? benchmark.batchSize() : 1;

        int repeatCount = repeatedTest != null ? repeatedTest.value() : 1;

        boolean needsTickWaiting = flowTest != null || asyncTest != null;

        return new TestMethodInfo(method, displayName, tags, orderValue,
                disabled != null, disabled != null ? disabled.value() : null,
                requiresWorld, worldName,
                requiresPlayer, playerCount,
                ecsTest != null, benchmark != null,
                warmup, iterations, batchSize,
                repeatedTest != null, repeatCount,
                parameterizedTest != null, timeoutMs,
                needsTickWaiting);
    }

    public int getTotalTestCount() { return registry.getTotalTestCount(); }
    public int getPluginCount() { return registry.getPluginCount(); }
}
