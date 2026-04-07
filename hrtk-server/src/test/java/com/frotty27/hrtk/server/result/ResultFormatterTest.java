package com.frotty27.hrtk.server.result;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultFormatterTest {

    private static TestResult makeResult(String name, TestStatus status, String message) {
        return new TestResult("TestPlugin", "TestSuite", name, name,
                status, 42L, message, message != null ? "at SomeClass.method()" : null, null);
    }

    private static SuiteResult makeSuite(List<TestResult> results) {
        return new SuiteResult("TestPlugin", "TestSuite", results, 100L);
    }

    @Nested
    class FormatRunStatuses {

        @Test
        void passingTestIncludesPass() {
            TestResult passed = makeResult("myTest", TestStatus.PASSED, null);
            String output = ResultFormatter.formatRun(List.of(makeSuite(List.of(passed))));
            assertTrue(output.contains("[PASS]"));
        }

        @Test
        void failingTestIncludesFailAndMessage() {
            TestResult failed = makeResult("myTest", TestStatus.FAILED, "expected true but was false");
            String output = ResultFormatter.formatRun(List.of(makeSuite(List.of(failed))));
            assertTrue(output.contains("[FAIL]"));
            assertTrue(output.contains("expected true but was false"));
        }

        @Test
        void erroredTestIncludesErr() {
            TestResult errored = makeResult("myTest", TestStatus.ERRORED, "NullPointerException");
            String output = ResultFormatter.formatRun(List.of(makeSuite(List.of(errored))));
            assertTrue(output.contains("[ERR ]"));
        }

        @Test
        void skippedTestIncludesSkip() {
            TestResult skipped = makeResult("myTest", TestStatus.SKIPPED, null);
            String output = ResultFormatter.formatRun(List.of(makeSuite(List.of(skipped))));
            assertTrue(output.contains("[SKIP]"));
        }

        @Test
        void timedOutTestIncludesTime() {
            TestResult timedOut = makeResult("myTest", TestStatus.TIMED_OUT, null);
            String output = ResultFormatter.formatRun(List.of(makeSuite(List.of(timedOut))));
            assertTrue(output.contains("[TIME]"));
        }
    }

    @Nested
    class FormatRunSummary {

        @Test
        void summaryShowsCorrectCounts() {
            TestResult passed = makeResult("test1", TestStatus.PASSED, null);
            TestResult failed = makeResult("test2", TestStatus.FAILED, "bad");
            TestResult skipped = makeResult("test3", TestStatus.SKIPPED, null);
            String output = ResultFormatter.formatRun(List.of(makeSuite(List.of(passed, failed, skipped))));
            assertTrue(output.contains("1 passed"));
            assertTrue(output.contains("1 failed"));
            assertTrue(output.contains("1 skipped"));
        }

        @Test
        void emptyResultsListDoesNotCrash() {
            String output = ResultFormatter.formatRun(List.of());
            assertNotNull(output);
            assertTrue(output.contains("0 passed"));
            assertTrue(output.contains("0 failed"));
            assertTrue(output.contains("0 skipped"));
        }
    }

    @Nested
    class FormatSingleResult {

        @Test
        void formatsPassedResultCorrectly() {
            TestResult passed = makeResult("myTest", TestStatus.PASSED, null);
            String output = ResultFormatter.formatSingleResult(passed);
            assertTrue(output.contains("[PASS]"));
            assertTrue(output.contains("myTest"));
            assertTrue(output.contains("42ms"));
        }

        @Test
        void formatsFailedResultWithMessage() {
            TestResult failed = makeResult("myTest", TestStatus.FAILED, "assertion error");
            String output = ResultFormatter.formatSingleResult(failed);
            assertTrue(output.contains("[FAIL]"));
            assertTrue(output.contains("assertion error"));
        }

        @Test
        void formatsPassedResultWithoutMessage() {
            TestResult passed = makeResult("myTest", TestStatus.PASSED, null);
            String output = ResultFormatter.formatSingleResult(passed);
            assertFalse(output.contains(" - "));
        }
    }
}
