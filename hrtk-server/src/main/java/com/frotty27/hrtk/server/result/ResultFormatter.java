package com.frotty27.hrtk.server.result;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;

import java.util.List;

public final class ResultFormatter {

    private ResultFormatter() {}

    public static String formatRun(List<SuiteResult> results) {
        StringBuilder output = new StringBuilder();
        long totalPassed = 0;
        long totalFailed = 0;
        long totalSkipped = 0;
        long totalDuration = 0;

        for (SuiteResult suite : results) {
            output.append("=== HRTK: ").append(suite.getPluginName()).append(" ===\n");
            output.append("  ").append(suite.getSuiteName()).append('\n');

            for (TestResult test : suite.getResults()) {
                output.append("    [").append(formatStatus(test.getStatus())).append("] ");
                output.append(test.getTestName());
                output.append(" (").append(test.getDurationMs()).append("ms)\n");

                if (test.getMessage() != null && test.failed()) {
                    output.append("           ").append(test.getMessage()).append('\n');
                }
            }

            totalPassed += suite.countPassed();
            totalFailed += suite.countFailed();
            totalSkipped += suite.countSkipped();
            totalDuration += suite.getTotalDurationMs();
        }

        output.append("Results: ").append(totalPassed).append(" passed, ");
        output.append(totalFailed).append(" failed, ");
        output.append(totalSkipped).append(" skipped (");
        output.append(totalDuration).append("ms total)\n");

        return output.toString();
    }

    public static String formatSingleResult(TestResult result) {
        StringBuilder output = new StringBuilder();
        output.append("[").append(formatStatus(result.getStatus())).append("] ");
        output.append(result.getTestName());
        output.append(" (").append(result.getDurationMs()).append("ms)");
        if (result.getMessage() != null && result.failed()) {
            output.append(" - ").append(result.getMessage());
        }
        return output.toString();
    }

    private static String formatStatus(TestStatus status) {
        return switch (status) {
            case PASSED -> "PASS";
            case FAILED -> "FAIL";
            case ERRORED -> "ERR ";
            case SKIPPED -> "SKIP";
            case TIMED_OUT -> "TIME";
        };
    }
}
