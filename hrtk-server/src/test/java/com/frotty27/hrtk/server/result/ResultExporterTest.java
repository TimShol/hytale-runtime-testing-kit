package com.frotty27.hrtk.server.result;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultExporterTest {

    private static TestResult makeTestResult(String name, TestStatus status, String message) {
        return new TestResult("TestPlugin", "TestSuite", name, "Display: " + name,
                status, 50L, message, message != null ? "stack" : null, List.of("unit"));
    }

    private static SuiteResult makeSuiteResult(List<TestResult> results) {
        return new SuiteResult("TestPlugin", "TestSuite", results, 200L);
    }

    private static String callJsonString(String value) {
        try {
            Method m = ResultExporter.class.getDeclaredMethod("jsonString", String.class);
            m.setAccessible(true);
            return (String) m.invoke(null, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class ExportToJson {

        @Test
        void writesValidJsonFile(@TempDir Path tempDir) throws IOException {
            TestResult passed = makeTestResult("testPass", TestStatus.PASSED, null);
            SuiteResult suite = makeSuiteResult(List.of(passed));

            Path file = ResultExporter.exportToJson(tempDir, List.of(suite));

            assertTrue(Files.exists(file));
            String content = Files.readString(file);
            assertTrue(content.startsWith("{"));
            assertTrue(content.trim().endsWith("}"));
        }

        @Test
        void containsTimestamp(@TempDir Path tempDir) throws IOException {
            TestResult passed = makeTestResult("testPass", TestStatus.PASSED, null);
            SuiteResult suite = makeSuiteResult(List.of(passed));

            Path file = ResultExporter.exportToJson(tempDir, List.of(suite));
            String content = Files.readString(file);

            assertTrue(content.contains("\"timestamp\""));
        }

        @Test
        void containsSuiteDataWithPluginAndSuiteNames(@TempDir Path tempDir) throws IOException {
            TestResult passed = makeTestResult("testPass", TestStatus.PASSED, null);
            SuiteResult suite = makeSuiteResult(List.of(passed));

            Path file = ResultExporter.exportToJson(tempDir, List.of(suite));
            String content = Files.readString(file);

            assertTrue(content.contains("\"plugin\""));
            assertTrue(content.contains("\"TestPlugin\""));
            assertTrue(content.contains("\"suite\""));
            assertTrue(content.contains("\"TestSuite\""));
        }

        @Test
        void containsTestDataWithNameStatusDurationMessage(@TempDir Path tempDir) throws IOException {
            TestResult failed = makeTestResult("testFail", TestStatus.FAILED, "expected 1 got 2");
            SuiteResult suite = makeSuiteResult(List.of(failed));

            Path file = ResultExporter.exportToJson(tempDir, List.of(suite));
            String content = Files.readString(file);

            assertTrue(content.contains("\"name\""));
            assertTrue(content.contains("\"testFail\""));
            assertTrue(content.contains("\"status\""));
            assertTrue(content.contains("\"FAILED\""));
            assertTrue(content.contains("\"durationMs\""));
            assertTrue(content.contains("\"message\""));
            assertTrue(content.contains("expected 1 got 2"));
        }

        @Test
        void handlesNullMessageForPassedTests(@TempDir Path tempDir) throws IOException {
            TestResult passed = makeTestResult("testPass", TestStatus.PASSED, null);
            SuiteResult suite = makeSuiteResult(List.of(passed));

            Path file = ResultExporter.exportToJson(tempDir, List.of(suite));
            String content = Files.readString(file);

            assertTrue(content.contains("\"message\": null"));
        }

        @Test
        void escapesSpecialCharacters(@TempDir Path tempDir) throws IOException {
            TestResult result = makeTestResult("testSpecial", TestStatus.FAILED,
                    "line1\nline2 with \"quotes\" and \\backslash");
            SuiteResult suite = makeSuiteResult(List.of(result));

            Path file = ResultExporter.exportToJson(tempDir, List.of(suite));
            String content = Files.readString(file);

            assertTrue(content.contains("\\n"));
            assertTrue(content.contains("\\\"quotes\\\""));
            assertTrue(content.contains("\\\\backslash"));
        }

        @Test
        void fileNameContainsTimestamp(@TempDir Path tempDir) throws IOException {
            TestResult passed = makeTestResult("testPass", TestStatus.PASSED, null);
            SuiteResult suite = makeSuiteResult(List.of(passed));

            Path file = ResultExporter.exportToJson(tempDir, List.of(suite));

            assertTrue(file.getFileName().toString().startsWith("run_"));
            assertTrue(file.getFileName().toString().endsWith(".json"));
        }

        @Test
        void emptyResultListWritesValidJson(@TempDir Path tempDir) throws IOException {
            Path file = ResultExporter.exportToJson(tempDir, List.of());
            String content = Files.readString(file);

            assertTrue(content.contains("\"suites\""));
            assertTrue(content.contains("\"suites\": ["));
            assertTrue(content.startsWith("{"));
            assertTrue(content.trim().endsWith("}"));
        }
    }

    @Nested
    class JsonStringHelper {

        @Test
        void handlesNullInput() {
            assertEquals("null", callJsonString(null));
        }

        @Test
        void escapesBackslashes() {
            String result = callJsonString("a\\b");
            assertEquals("\"a\\\\b\"", result);
        }

        @Test
        void escapesQuotes() {
            String result = callJsonString("say \"hello\"");
            assertEquals("\"say \\\"hello\\\"\"", result);
        }

        @Test
        void escapesNewlines() {
            String result = callJsonString("line1\nline2");
            assertEquals("\"line1\\nline2\"", result);
        }

        @Test
        void wrapsPlainStringInQuotes() {
            String result = callJsonString("hello");
            assertEquals("\"hello\"", result);
        }
    }
}
