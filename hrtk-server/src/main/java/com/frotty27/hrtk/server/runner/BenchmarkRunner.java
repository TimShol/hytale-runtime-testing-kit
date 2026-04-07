package com.frotty27.hrtk.server.runner;

import com.frotty27.hrtk.api.assert_.AssertionFailedException;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;
import com.frotty27.hrtk.server.context.LiveBenchmarkContext;
import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestMethodInfo;
import com.hypixel.hytale.common.benchmark.TimeRecorder;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class BenchmarkRunner {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public TestResult runBenchmark(TestClassInfo suite, TestMethodInfo methodInfo, Object suiteInstance) {
        String pluginName = suite.getPluginName();
        String suiteName = suite.getSuiteName();
        String testName = methodInfo.getMethod().getName();
        String displayName = methodInfo.getDisplayName();
        List<String> allTags = new ArrayList<>(suite.getClassTags());
        allTags.addAll(methodInfo.getTags());

        int warmupIterations = methodInfo.getBenchmarkWarmup();
        int measureIterations = methodInfo.getBenchmarkIterations();
        int batchSize = methodInfo.getBenchmarkBatchSize();

        LiveBenchmarkContext context = new LiveBenchmarkContext(pluginName, measureIterations);
        Method method = methodInfo.getMethod();

        Object[] params = buildParams(method, context);

        long totalStartMs = System.currentTimeMillis();

        try {
            context.setWarmup(true);
            boolean manualTiming = false;
            for (int i = 0; i < warmupIterations; i++) {
                context.setIteration(i);
                context.resetManualTiming();
                method.invoke(suiteInstance, params);
                if (i == 0 && context.isManualTiming()) {
                    manualTiming = true;
                }
            }

            context.setWarmup(false);
            for (int i = 0; i < measureIterations; i++) {
                context.setIteration(i);
                context.resetManualTiming();

                if (manualTiming) {
                    method.invoke(suiteInstance, params);
                    if (context.isManualTiming()) {
                        context.stopTimer();
                    }
                } else {
                    long startNanos = System.nanoTime();
                    method.invoke(suiteInstance, params);
                    context.endAutoTimer(startNanos);
                }
            }

            long totalDurationMs = System.currentTimeMillis() - totalStartMs;

            TimeRecorder recorder = context.getTimeRecorder();
            long count = recorder.getCount();
            String stats = formatBenchmarkStats(recorder, batchSize, warmupIterations, measureIterations);

            LOGGER.atInfo().log("HRTK [BENCH] %s.%s: %s", suiteName, displayName, stats);

            return new TestResult(pluginName, suiteName, testName, displayName,
                    TestStatus.PASSED, totalDurationMs, stats, null, allTags);

        } catch (Throwable thrown) {
            long totalDurationMs = System.currentTimeMillis() - totalStartMs;
            Throwable cause = thrown.getCause() != null ? thrown.getCause() : thrown;

            if (cause instanceof AssertionFailedException) {
                return new TestResult(pluginName, suiteName, testName, displayName,
                        TestStatus.FAILED, totalDurationMs, cause.getMessage(),
                        formatStackTrace(cause), allTags);
            }
            return new TestResult(pluginName, suiteName, testName, displayName,
                    TestStatus.ERRORED, totalDurationMs,
                    cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                    formatStackTrace(cause), allTags);
        }
    }

    private String formatBenchmarkStats(TimeRecorder recorder, int batchSize,
                                         int warmupIterations, int measureIterations) {
        double avg = recorder.getAverage();
        double min = recorder.getMinValue();
        double max = recorder.getMaxValue();
        long count = recorder.getCount();

        String avgStr = TimeRecorder.formatTime(avg);
        String minStr = TimeRecorder.formatTime(min);
        String maxStr = TimeRecorder.formatTime(max);

        long totalOps = count * batchSize;
        double opsPerSec = avg > 0 ? batchSize / avg : 0;

        StringBuilder output = new StringBuilder();
        output.append(String.format("avg=%s, min=%s, max=%s", avgStr, minStr, maxStr));
        output.append(String.format(" (%d iterations, %d warmup", measureIterations, warmupIterations));
        if (batchSize > 1) {
            output.append(String.format(", %d ops/iter, %.0f ops/sec", batchSize, opsPerSec));
        }
        output.append(')');
        return output.toString();
    }

    private Object[] buildParams(Method method, LiveBenchmarkContext context) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i].isAssignableFrom(LiveBenchmarkContext.class)
                    || "BenchmarkContext".equals(paramTypes[i].getSimpleName())) {
                params[i] = context;
            } else {
                params[i] = null;
            }
        }
        return params;
    }

    private String formatStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
