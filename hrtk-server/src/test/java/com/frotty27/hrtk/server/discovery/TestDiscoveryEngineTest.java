package com.frotty27.hrtk.server.discovery;

import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TestDiscoveryEngineTest {

    private TestDiscoveryEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TestDiscoveryEngine();
    }

    private TestClassInfo invokeAnalyzeClass(Class<?> clazz, String pluginName) throws Exception {
        Method method = TestDiscoveryEngine.class.getDeclaredMethod("analyzeClass", Class.class, String.class);
        method.setAccessible(true);
        return (TestClassInfo) method.invoke(engine, clazz, pluginName);
    }

    private TestMethodInfo findMethod(TestClassInfo info, String methodName) {
        return info.getTestMethods().stream()
                .filter(m -> m.getMethod().getName().equals(methodName))
                .findFirst()
                .orElse(null);
    }

    @HytaleSuite("My Suite")
    @Tag("integration")
    static class AnnotatedSuite {
        @HytaleTest
        void simpleTest() {}

        @HytaleTest("Custom Name")
        @Order(1)
        void namedTest() {}

        @EcsTest
        void ecsTest() {}

        @WorldTest
        void worldTest() {}

        @WorldTest(world = "custom_world")
        void worldTestWithName() {}

        @CombatTest
        void combatTest() {}

        @FlowTest(timeoutTicks = 100)
        void flowTest() {}

        @Benchmark(warmup = 10, iterations = 100, batchSize = 5)
        void benchmarkTest() {}

        @Disabled("not ready")
        @HytaleTest
        void disabledTest() {}

        @RepeatedTest(5)
        void repeatedTest() {}

        @ParameterizedTest
        void paramTest() {}

        @HytaleTest
        @Tag("unit")
        void taggedTest() {}

        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @HytaleTest
        void timeoutTest() {}

        @DisplayName("Pretty Name")
        @HytaleTest
        void displayNameTest() {}

        @AsyncTest(timeoutTicks = 300)
        void asyncTest() {}

        @com.frotty27.hrtk.api.annotation.BeforeAll
        static void beforeAll() {}

        @com.frotty27.hrtk.api.annotation.AfterAll
        static void afterAll() {}

        @com.frotty27.hrtk.api.annotation.BeforeEach
        void beforeEach() {}

        @com.frotty27.hrtk.api.annotation.AfterEach
        void afterEach() {}
    }

    static class NoAnnotations {
        void notATest() {}
    }

    @HytaleSuite("Disabled Suite")
    @Disabled("whole suite disabled")
    static class DisabledSuite {
        @HytaleTest
        void test() {}
    }

    @HytaleSuite(value = "Isolated Suite", isolation = IsolationStrategy.DEDICATED_WORLD)
    static class IsolatedSuite {
        @HytaleTest
        void test() {}
    }

    static class NoSuiteAnnotation {
        @HytaleTest
        void test() {}
    }

    @HytaleSuite("Ordered Suite")
    static class OrderedSuite {
        @HytaleTest
        @Order(3)
        void third() {}

        @HytaleTest
        @Order(1)
        void first() {}

        @HytaleTest
        @Order(2)
        void second() {}

        @HytaleTest
        void unordered() {}
    }

    @Tag({"ecs"})
    static class EcsTaggedClass {
        @EcsTest
        void ecsMethod() {}
    }

    @HytaleSuite("Combat World Suite")
    static class CombatWorldSuite {
        @CombatTest(world = "arena_01")
        void combatInArena() {}
    }

    @HytaleSuite("Flow World Suite")
    static class FlowWorldSuite {
        @FlowTest(world = "dungeon_01")
        void flowInDungeon() {}
    }

    @Nested
    class AnalyzeClassTests {

        @Test
        void classWithHytaleSuite_usesSuiteNameFromAnnotation() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
            assertEquals("My Suite", info.getSuiteName());
        }

        @Test
        void classWithoutHytaleSuite_usesClassSimpleName() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(NoSuiteAnnotation.class, "TestPlugin");
            assertEquals("NoSuiteAnnotation", info.getSuiteName());
        }

        @Test
        void classWithNoTestMethods_returnsNull() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(NoAnnotations.class, "TestPlugin");
            assertNull(info);
        }

        @Test
        void classWithDisabled_marksClassDisabledWithReason() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(DisabledSuite.class, "TestPlugin");
            assertTrue(info.isClassDisabled());
            assertEquals("whole suite disabled", info.getClassDisabledReason());
        }

        @Test
        void classWithTag_storesClassTags() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
            assertTrue(info.getClassTags().contains("integration"));
        }

        @Test
        void classWithLifecycleMethods_capturesBeforeAllAndAfterAll() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
            assertFalse(info.getBeforeAllMethods().isEmpty());
            assertTrue(info.getBeforeAllMethods().stream()
                    .anyMatch(m -> m.getName().equals("beforeAll")));
            assertFalse(info.getAfterAllMethods().isEmpty());
            assertTrue(info.getAfterAllMethods().stream()
                    .anyMatch(m -> m.getName().equals("afterAll")));
        }

        @Test
        void classWithLifecycleMethods_capturesBeforeEachAndAfterEach() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
            assertFalse(info.getBeforeEachMethods().isEmpty());
            assertTrue(info.getBeforeEachMethods().stream()
                    .anyMatch(m -> m.getName().equals("beforeEach")));
            assertFalse(info.getAfterEachMethods().isEmpty());
            assertTrue(info.getAfterEachMethods().stream()
                    .anyMatch(m -> m.getName().equals("afterEach")));
        }

        @Test
        void classWithIsolationStrategy_capturesIsolation() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(IsolatedSuite.class, "TestPlugin");
            assertEquals(IsolationStrategy.DEDICATED_WORLD, info.getIsolation());
        }

        @Test
        void classWithoutIsolationStrategy_defaultsToNone() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(NoSuiteAnnotation.class, "TestPlugin");
            assertEquals(IsolationStrategy.NONE, info.getIsolation());
        }

        @Test
        void classStoresPluginName() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "MyPlugin");
            assertEquals("MyPlugin", info.getPluginName());
        }

        @Test
        void classStoresTestClassReference() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
            assertEquals(AnnotatedSuite.class, info.getTestClass());
        }

        @Test
        void classReportsCorrectTestCount() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
            assertTrue(info.getTestCount() > 0);
        }

        @Test
        void testMethodsSortedByOrder() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(OrderedSuite.class, "TestPlugin");
            List<TestMethodInfo> methods = info.getTestMethods();
            assertEquals("first", methods.get(0).getMethod().getName());
            assertEquals("second", methods.get(1).getMethod().getName());
            assertEquals("third", methods.get(2).getMethod().getName());
            assertEquals("unordered", methods.get(3).getMethod().getName());
        }

        @Test
        void classNotDisabled_classDisabledIsFalse() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
            assertFalse(info.isClassDisabled());
            assertNull(info.getClassDisabledReason());
        }
    }

    @Nested
    class AnalyzeMethodTests {

        private TestClassInfo suiteInfo;

        @BeforeEach
        void setUp() throws Exception {
            suiteInfo = invokeAnalyzeClass(AnnotatedSuite.class, "TestPlugin");
        }

        @Test
        void hytaleTestMethod_isDiscovered() {
            TestMethodInfo info = findMethod(suiteInfo, "simpleTest");
            assertNotNull(info);
            assertEquals("simpleTest", info.getDisplayName());
        }

        @Test
        void hytaleTestWithCustomName_setsDisplayName() {
            TestMethodInfo info = findMethod(suiteInfo, "namedTest");
            assertNotNull(info);
            assertEquals("Custom Name", info.getDisplayName());
        }

        @Test
        void ecsTestMethod_isDiscoveredWithEcsTag() {
            TestMethodInfo info = findMethod(suiteInfo, "ecsTest");
            assertNotNull(info);
            assertTrue(info.getTags().contains("ecs"));
            assertTrue(info.isEcsTest());
            assertTrue(info.requiresWorld());
        }

        @Test
        void worldTestMethod_isDiscoveredWithIntegrationTag() {
            TestMethodInfo info = findMethod(suiteInfo, "worldTest");
            assertNotNull(info);
            assertTrue(info.getTags().contains("integration"));
            assertTrue(info.requiresWorld());
        }

        @Test
        void worldTestWithWorldName_capturesWorldName() {
            TestMethodInfo info = findMethod(suiteInfo, "worldTestWithName");
            assertNotNull(info);
            assertTrue(info.requiresWorld());
            assertEquals("custom_world", info.getWorldName());
        }

        @Test
        void combatTestMethod_isDiscoveredWithCombatTag() {
            TestMethodInfo info = findMethod(suiteInfo, "combatTest");
            assertNotNull(info);
            assertTrue(info.getTags().contains("combat"));
            assertTrue(info.requiresWorld());
        }

        @Test
        void combatTestWithWorld_capturesWorldName() throws Exception {
            TestClassInfo combatInfo = invokeAnalyzeClass(CombatWorldSuite.class, "TestPlugin");
            TestMethodInfo info = findMethod(combatInfo, "combatInArena");
            assertNotNull(info);
            assertEquals("arena_01", info.getWorldName());
        }

        @Test
        void flowTestMethod_isDiscoveredWithFlowTag() {
            TestMethodInfo info = findMethod(suiteInfo, "flowTest");
            assertNotNull(info);
            assertTrue(info.getTags().contains("flow"));
            assertTrue(info.requiresWorld());
        }

        @Test
        void flowTestTimeoutTicks_convertedToMilliseconds() {
            TestMethodInfo info = findMethod(suiteInfo, "flowTest");
            assertNotNull(info);
            assertEquals(5000L, info.getTimeoutMs());
        }

        @Test
        void flowTestWithWorld_capturesWorldName() throws Exception {
            TestClassInfo flowInfo = invokeAnalyzeClass(FlowWorldSuite.class, "TestPlugin");
            TestMethodInfo info = findMethod(flowInfo, "flowInDungeon");
            assertNotNull(info);
            assertEquals("dungeon_01", info.getWorldName());
        }

        @Test
        void asyncTestMethod_isDiscoveredWithAsyncTag() {
            TestMethodInfo info = findMethod(suiteInfo, "asyncTest");
            assertNotNull(info);
            assertTrue(info.getTags().contains("async"));
        }

        @Test
        void asyncTestTimeoutTicks_convertedToMilliseconds() {
            TestMethodInfo info = findMethod(suiteInfo, "asyncTest");
            assertNotNull(info);
            assertEquals(15000L, info.getTimeoutMs());
        }

        @Test
        void benchmarkMethod_isBenchmarkWithParams() {
            TestMethodInfo info = findMethod(suiteInfo, "benchmarkTest");
            assertNotNull(info);
            assertTrue(info.isBenchmark());
            assertEquals(10, info.getBenchmarkWarmup());
            assertEquals(100, info.getBenchmarkIterations());
            assertEquals(5, info.getBenchmarkBatchSize());
        }

        @Test
        void disabledMethod_isDisabledWithReason() {
            TestMethodInfo info = findMethod(suiteInfo, "disabledTest");
            assertNotNull(info);
            assertTrue(info.isDisabled());
            assertEquals("not ready", info.getDisabledReason());
        }

        @Test
        void orderAnnotation_capturesOrderValue() {
            TestMethodInfo info = findMethod(suiteInfo, "namedTest");
            assertNotNull(info);
            assertEquals(1, info.getOrder());
        }

        @Test
        void methodWithoutOrder_hasMaxIntOrder() {
            TestMethodInfo info = findMethod(suiteInfo, "simpleTest");
            assertNotNull(info);
            assertEquals(Integer.MAX_VALUE, info.getOrder());
        }

        @Test
        void repeatedTestMethod_isRepeatedWithCount() {
            TestMethodInfo info = findMethod(suiteInfo, "repeatedTest");
            assertNotNull(info);
            assertTrue(info.isRepeatedTest());
            assertEquals(5, info.getRepeatCount());
        }

        @Test
        void parameterizedTestMethod_isParameterized() {
            TestMethodInfo info = findMethod(suiteInfo, "paramTest");
            assertNotNull(info);
            assertTrue(info.isParameterized());
        }

        @Test
        void tagAnnotationOnMethod_capturesTag() {
            TestMethodInfo info = findMethod(suiteInfo, "taggedTest");
            assertNotNull(info);
            assertTrue(info.getTags().contains("unit"));
        }

        @Test
        void timeoutAnnotation_convertedToMilliseconds() {
            TestMethodInfo info = findMethod(suiteInfo, "timeoutTest");
            assertNotNull(info);
            assertEquals(5000L, info.getTimeoutMs());
        }

        @Test
        void displayNameAnnotation_overridesMethodName() {
            TestMethodInfo info = findMethod(suiteInfo, "displayNameTest");
            assertNotNull(info);
            assertEquals("Pretty Name", info.getDisplayName());
        }

        @Test
        void methodWithNoTestAnnotation_notDiscovered() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(NoAnnotations.class, "TestPlugin");
            assertNull(info);
        }

        @Test
        void defaultTimeoutForSimpleTest_is30Seconds() {
            TestMethodInfo info = findMethod(suiteInfo, "simpleTest");
            assertNotNull(info);
            assertEquals(30000L, info.getTimeoutMs());
        }

        @Test
        void nonBenchmarkMethod_hasZeroBenchmarkParams() {
            TestMethodInfo info = findMethod(suiteInfo, "simpleTest");
            assertNotNull(info);
            assertFalse(info.isBenchmark());
            assertEquals(0, info.getBenchmarkWarmup());
            assertEquals(0, info.getBenchmarkIterations());
            assertEquals(1, info.getBenchmarkBatchSize());
        }

        @Test
        void nonRepeatedMethod_hasRepeatCountOne() {
            TestMethodInfo info = findMethod(suiteInfo, "simpleTest");
            assertNotNull(info);
            assertFalse(info.isRepeatedTest());
            assertEquals(1, info.getRepeatCount());
        }

        @Test
        void nonDisabledMethod_hasNullReason() {
            TestMethodInfo info = findMethod(suiteInfo, "simpleTest");
            assertNotNull(info);
            assertFalse(info.isDisabled());
            assertNull(info.getDisabledReason());
        }

        @Test
        void simpleTestMethod_doesNotRequireWorld() {
            TestMethodInfo info = findMethod(suiteInfo, "simpleTest");
            assertNotNull(info);
            assertFalse(info.requiresWorld());
        }

        @Test
        void ecsTagNotDuplicated_whenClassAlsoHasEcsTag() throws Exception {
            TestClassInfo info = invokeAnalyzeClass(EcsTaggedClass.class, "TestPlugin");
            TestMethodInfo methodInfo = findMethod(info, "ecsMethod");
            assertNotNull(methodInfo);
            long ecsTagCount = methodInfo.getTags().stream().filter("ecs"::equals).count();
            assertEquals(1, ecsTagCount);
        }
    }

    @Nested
    class PluginTestRegistryTests {

        private PluginTestRegistry registry;

        @BeforeEach
        void setUp() {
            registry = new PluginTestRegistry();
        }

        private TestClassInfo createDummyInfo(String suiteName, String pluginName, int methodCount) throws Exception {
            return invokeAnalyzeClass(AnnotatedSuite.class, pluginName);
        }

        @Test
        void registerAndGetTestClasses() throws Exception {
            TestClassInfo info = createDummyInfo("Suite", "PluginA", 1);
            registry.register("PluginA", info);
            List<TestClassInfo> classes = registry.getTestClasses("PluginA");
            assertEquals(1, classes.size());
            assertSame(info, classes.get(0));
        }

        @Test
        void clearPlugin_removesTestsForThatPlugin() throws Exception {
            TestClassInfo infoA = createDummyInfo("Suite A", "PluginA", 1);
            TestClassInfo infoB = createDummyInfo("Suite B", "PluginB", 1);
            registry.register("PluginA", infoA);
            registry.register("PluginB", infoB);
            registry.clearPlugin("PluginA");
            assertTrue(registry.getTestClasses("PluginA").isEmpty());
            assertEquals(1, registry.getTestClasses("PluginB").size());
        }

        @Test
        void clear_removesEverything() throws Exception {
            TestClassInfo infoA = createDummyInfo("Suite A", "PluginA", 1);
            TestClassInfo infoB = createDummyInfo("Suite B", "PluginB", 1);
            registry.register("PluginA", infoA);
            registry.register("PluginB", infoB);
            registry.clear();
            assertTrue(registry.getTestClasses("PluginA").isEmpty());
            assertTrue(registry.getTestClasses("PluginB").isEmpty());
            assertEquals(0, registry.getPluginCount());
        }

        @Test
        void getTotalTestCount_sumsAllTests() throws Exception {
            TestClassInfo infoA = createDummyInfo("Suite A", "PluginA", 1);
            TestClassInfo infoB = createDummyInfo("Suite B", "PluginB", 1);
            registry.register("PluginA", infoA);
            registry.register("PluginB", infoB);
            assertEquals(infoA.getTestCount() + infoB.getTestCount(), registry.getTotalTestCount());
        }

        @Test
        void getPluginCount_returnsNumberOfPlugins() throws Exception {
            registry.register("PluginA", createDummyInfo("Suite A", "PluginA", 1));
            registry.register("PluginB", createDummyInfo("Suite B", "PluginB", 1));
            assertEquals(2, registry.getPluginCount());
        }

        @Test
        void getTotalSuiteCount_returnsNumberOfSuites() throws Exception {
            TestClassInfo info1 = createDummyInfo("Suite 1", "PluginA", 1);
            TestClassInfo info2 = createDummyInfo("Suite 2", "PluginA", 1);
            TestClassInfo info3 = createDummyInfo("Suite 3", "PluginB", 1);
            registry.register("PluginA", info1);
            registry.register("PluginA", info2);
            registry.register("PluginB", info3);
            assertEquals(3, registry.getTotalSuiteCount());
        }

        @Test
        void getTestClasses_forUnknownPlugin_returnsEmptyList() {
            List<TestClassInfo> classes = registry.getTestClasses("NonExistent");
            assertNotNull(classes);
            assertTrue(classes.isEmpty());
        }

        @Test
        void getPluginNames_returnsAllRegisteredPlugins() throws Exception {
            registry.register("PluginA", createDummyInfo("Suite A", "PluginA", 1));
            registry.register("PluginB", createDummyInfo("Suite B", "PluginB", 1));
            List<String> names = registry.getPluginNames();
            assertEquals(2, names.size());
            assertTrue(names.contains("PluginA"));
            assertTrue(names.contains("PluginB"));
        }

        @Test
        void getAllTestClasses_returnsUnmodifiableMap() throws Exception {
            registry.register("PluginA", createDummyInfo("Suite A", "PluginA", 1));
            assertThrows(UnsupportedOperationException.class,
                    () -> registry.getAllTestClasses().put("X", List.of()));
        }

        @Test
        void emptyRegistry_countsAreZero() {
            assertEquals(0, registry.getTotalTestCount());
            assertEquals(0, registry.getPluginCount());
            assertEquals(0, registry.getTotalSuiteCount());
        }
    }
}
