package com.frotty27.hrtk.server.result;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResultCollector {

    private final List<SuiteResult> currentResults = new ArrayList<>();
    private final List<RunRecord> history = new ArrayList<>();
    private long currentRunStart;

    public synchronized void startRun() {
        currentResults.clear();
        currentRunStart = System.currentTimeMillis();
    }

    public synchronized void addResult(SuiteResult result) {
        currentResults.add(result);
    }

    public synchronized RunRecord finishRun() {
        long duration = System.currentTimeMillis() - currentRunStart;
        String id = "run_" + currentRunStart;
        RunRecord record = new RunRecord(id, currentRunStart, List.copyOf(currentResults), duration);
        history.add(record);
        while (history.size() > 50) {
            history.removeFirst();
        }
        return record;
    }

    public synchronized List<RunRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public synchronized RunRecord getLastRun() {
        return history.isEmpty() ? null : history.getLast();
    }

    public synchronized List<SuiteResult> getCurrentResults() {
        return Collections.unmodifiableList(currentResults);
    }

    public record RunRecord(String id, long timestamp, List<SuiteResult> results, long totalDurationMs) {
        public long countPassed() { return results.stream().mapToLong(SuiteResult::countPassed).sum(); }
        public long countFailed() { return results.stream().mapToLong(SuiteResult::countFailed).sum(); }
        public long countSkipped() { return results.stream().mapToLong(SuiteResult::countSkipped).sum(); }
    }
}
