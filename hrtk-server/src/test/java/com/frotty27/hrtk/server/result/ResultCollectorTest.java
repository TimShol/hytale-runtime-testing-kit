package com.frotty27.hrtk.server.result;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultCollectorTest {

    private ResultCollector collector;

    @BeforeEach
    void setUp() {
        collector = new ResultCollector();
    }

    private static SuiteResult makeSuiteResult(int passed, int failed, int skipped) {
        List<TestResult> results = new java.util.ArrayList<>();
        for (int i = 0; i < passed; i++) {
            results.add(new TestResult("plugin", "suite", "pass" + i, "Pass " + i,
                    TestStatus.PASSED, 10L, null, null, null));
        }
        for (int i = 0; i < failed; i++) {
            results.add(new TestResult("plugin", "suite", "fail" + i, "Fail " + i,
                    TestStatus.FAILED, 10L, "assertion failed", "stack", null));
        }
        for (int i = 0; i < skipped; i++) {
            results.add(new TestResult("plugin", "suite", "skip" + i, "Skip " + i,
                    TestStatus.SKIPPED, 0L, null, null, null));
        }
        return new SuiteResult("plugin", "suite", results, 100L);
    }

    @Nested
    class StartAndFinishRun {

        @Test
        void startRunThenFinishRunProducesRunRecord() {
            collector.startRun();
            ResultCollector.RunRecord record = collector.finishRun();
            assertNotNull(record);
            assertNotNull(record.id());
            assertTrue(record.id().startsWith("run_"));
        }

        @Test
        void finishRunCalculatesCorrectDuration() {
            collector.startRun();
            ResultCollector.RunRecord record = collector.finishRun();
            assertTrue(record.totalDurationMs() >= 0,
                    "Duration should be non-negative");
        }
    }

    @Nested
    class AddResult {

        @Test
        void addResultAddsToCurrentResults() {
            collector.startRun();
            SuiteResult result = makeSuiteResult(1, 0, 0);
            collector.addResult(result);
            assertEquals(1, collector.getCurrentResults().size());
        }

        @Test
        void getCurrentResultsDuringRunReturnsCurrentResults() {
            collector.startRun();
            SuiteResult result1 = makeSuiteResult(1, 0, 0);
            SuiteResult result2 = makeSuiteResult(0, 1, 0);
            collector.addResult(result1);
            collector.addResult(result2);
            assertEquals(2, collector.getCurrentResults().size());
        }
    }

    @Nested
    class GetLastRun {

        @Test
        void returnsNullBeforeAnyRun() {
            assertNull(collector.getLastRun());
        }

        @Test
        void returnsMostRecentRun() {
            collector.startRun();
            collector.addResult(makeSuiteResult(1, 0, 0));
            collector.finishRun();

            collector.startRun();
            collector.addResult(makeSuiteResult(0, 1, 0));
            ResultCollector.RunRecord lastRecord = collector.finishRun();

            assertEquals(lastRecord, collector.getLastRun());
        }
    }

    @Nested
    class GetHistory {

        @Test
        void returnsAllRunsInOrder() {
            collector.startRun();
            collector.finishRun();

            collector.startRun();
            collector.finishRun();

            collector.startRun();
            collector.finishRun();

            List<ResultCollector.RunRecord> history = collector.getHistory();
            assertEquals(3, history.size());
            for (int i = 0; i < history.size() - 1; i++) {
                assertTrue(history.get(i).timestamp() <= history.get(i + 1).timestamp(),
                        "History should be in chronological order");
            }
        }

        @Test
        void historyLimitKeepsOnly50Runs() {
            for (int i = 0; i < 51; i++) {
                collector.startRun();
                collector.finishRun();
            }

            assertEquals(50, collector.getHistory().size(),
                    "History should be capped at 50 runs");
        }
    }

    @Nested
    class RunRecordCounts {

        @Test
        void countPassedFailedSkippedAreCorrect() {
            collector.startRun();
            collector.addResult(makeSuiteResult(3, 2, 1));
            collector.addResult(makeSuiteResult(1, 0, 2));
            ResultCollector.RunRecord record = collector.finishRun();

            assertEquals(4, record.countPassed());
            assertEquals(2, record.countFailed());
            assertEquals(3, record.countSkipped());
        }
    }

    @Nested
    class StartRunClears {

        @Test
        void startRunClearsPreviousCurrentResults() {
            collector.startRun();
            collector.addResult(makeSuiteResult(1, 0, 0));
            assertEquals(1, collector.getCurrentResults().size());

            collector.startRun();
            assertEquals(0, collector.getCurrentResults().size(),
                    "startRun should clear previous current results");
        }
    }
}
