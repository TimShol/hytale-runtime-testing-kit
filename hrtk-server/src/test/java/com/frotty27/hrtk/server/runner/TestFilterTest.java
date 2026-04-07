package com.frotty27.hrtk.server.runner;

import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestMethodInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestFilterTest {

    @SuppressWarnings("unused")
    static class DummySuite {
        void testMethod() {}
        void benchmarkMethod() {}
        void anotherMethod() {}
    }

    private static Method dummyMethod(String name) {
        try {
            return DummySuite.class.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static TestClassInfo makeSuite(String pluginName, String suiteName, List<String> classTags) {
        return new TestClassInfo(
                DummySuite.class, suiteName, pluginName,
                IsolationStrategy.NONE, classTags,
                false, null,
                List.of(), List.of(), List.of(), List.of(), List.of()
        );
    }

    private static TestMethodInfo makeMethod(String methodName, List<String> tags, boolean isBenchmark) {
        return new TestMethodInfo(
                dummyMethod(methodName), methodName, tags, 0,
                false, null,
                false, null,
                false, 0,
                false, isBenchmark,
                0, 0, 0,
                false, 0,
                false, 0L,
                false
        );
    }

    @Nested
    class AllFactory {

        @Test
        void matchesAnySuite() {
            TestFilter filter = TestFilter.all();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            assertTrue(filter.matchesSuite(suite));
        }

        @Test
        void matchesAnyMethod() {
            TestFilter filter = TestFilter.all();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo method = makeMethod("testMethod", List.of(), false);
            assertTrue(filter.matchesMethod(suite, method));
        }
    }

    @Nested
    class PluginFilter {

        @Test
        void matchesCorrectPluginCaseInsensitive() {
            TestFilter filter = TestFilter.builder().plugin("MyPlugin").build();
            TestClassInfo suite = makeSuite("myplugin", "SomeSuite", List.of());
            assertTrue(filter.matchesSuite(suite));
        }

        @Test
        void rejectsOtherPluginNames() {
            TestFilter filter = TestFilter.builder().plugin("MyPlugin").build();
            TestClassInfo suite = makeSuite("OtherPlugin", "SomeSuite", List.of());
            assertFalse(filter.matchesSuite(suite));
        }
    }

    @Nested
    class SuiteFilter {

        @Test
        void matchesCorrectSuiteCaseInsensitive() {
            TestFilter filter = TestFilter.builder().suite("MySuite").build();
            TestClassInfo suite = makeSuite("AnyPlugin", "mysuite", List.of());
            assertTrue(filter.matchesSuite(suite));
        }

        @Test
        void rejectsOtherSuiteNames() {
            TestFilter filter = TestFilter.builder().suite("MySuite").build();
            TestClassInfo suite = makeSuite("AnyPlugin", "OtherSuite", List.of());
            assertFalse(filter.matchesSuite(suite));
        }
    }

    @Nested
    class MethodFilter {

        @Test
        void matchesCorrectMethodName() {
            TestFilter filter = TestFilter.builder().method("testMethod").build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo method = makeMethod("testMethod", List.of(), false);
            assertTrue(filter.matchesMethod(suite, method));
        }

        @Test
        void rejectsOtherMethodNames() {
            TestFilter filter = TestFilter.builder().method("testMethod").build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo method = makeMethod("anotherMethod", List.of(), false);
            assertFalse(filter.matchesMethod(suite, method));
        }
    }

    @Nested
    class TagFilter {

        @Test
        void matchesMethodWithTag() {
            TestFilter filter = TestFilter.builder().tags(List.of("unit")).build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo method = makeMethod("testMethod", List.of("unit"), false);
            assertTrue(filter.matchesMethod(suite, method));
        }

        @Test
        void matchesClassWithTag() {
            TestFilter filter = TestFilter.builder().tags(List.of("unit")).build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of("unit"));
            TestMethodInfo method = makeMethod("testMethod", List.of(), false);
            assertTrue(filter.matchesMethod(suite, method));
        }

        @Test
        void doesNotRejectSuiteLackingClassTagWhenMethodHasTag() {
            TestFilter filter = TestFilter.builder().tags(List.of("unit")).build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo method = makeMethod("testMethod", List.of("unit"), false);
            assertTrue(filter.matchesMethod(suite, method),
                    "Suite without class-level tag should still match if method has the tag");
        }

        @Test
        void rejectsWhenNeitherClassNorMethodHasTag() {
            TestFilter filter = TestFilter.builder().tags(List.of("unit")).build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo method = makeMethod("testMethod", List.of(), false);
            assertFalse(filter.matchesMethod(suite, method));
        }
    }

    @Nested
    class BenchmarkFilter {

        @Test
        void matchesBenchmarkMethodsOnly() {
            TestFilter filter = TestFilter.builder().benchmarkOnly(true).build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo benchmark = makeMethod("benchmarkMethod", List.of(), true);
            assertTrue(filter.matchesMethod(suite, benchmark));
        }

        @Test
        void rejectsNonBenchmarkMethods() {
            TestFilter filter = TestFilter.builder().benchmarkOnly(true).build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo regular = makeMethod("testMethod", List.of(), false);
            assertFalse(filter.matchesMethod(suite, regular));
        }
    }

    @Nested
    class FailFastFilter {

        @Test
        void failFastIsTrue() {
            TestFilter filter = TestFilter.builder().failFast(true).build();
            assertTrue(filter.isFailFast());
        }

        @Test
        void failFastDefaultIsFalse() {
            TestFilter filter = TestFilter.builder().build();
            assertFalse(filter.isFailFast());
        }
    }

    @Nested
    class CombinedFilter {

        @Test
        void pluginAndTagAndFailFastWorkTogether() {
            TestFilter filter = TestFilter.builder()
                    .plugin("MyPlugin")
                    .tags(List.of("integration"))
                    .failFast(true)
                    .build();

            TestClassInfo matchingSuite = makeSuite("MyPlugin", "AnySuite", List.of("integration"));
            TestMethodInfo method = makeMethod("testMethod", List.of(), false);

            assertTrue(filter.isFailFast());
            assertTrue(filter.matchesMethod(matchingSuite, method));

            TestClassInfo wrongPlugin = makeSuite("OtherPlugin", "AnySuite", List.of("integration"));
            assertFalse(filter.matchesMethod(wrongPlugin, method));
        }
    }

    @Nested
    class EmptyFilter {

        @Test
        void emptyBuilderMatchesEverything() {
            TestFilter filter = TestFilter.builder().build();
            TestClassInfo suite = makeSuite("AnyPlugin", "AnySuite", List.of());
            TestMethodInfo method = makeMethod("testMethod", List.of(), false);
            assertTrue(filter.matchesSuite(suite));
            assertTrue(filter.matchesMethod(suite, method));
        }
    }
}
