package com.frotty27.hrtk.server.runner;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestRunListener;
import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestDiscoveryEngine;
import com.frotty27.hrtk.server.isolation.TestWorldManager;
import com.frotty27.hrtk.server.result.ResultCollector;
import com.frotty27.hrtk.server.result.ResultFormatter;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TestRunner {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final TestDiscoveryEngine discovery;
    private final ResultCollector collector;
    private final TestExecutor testExecutor;
    private final SuiteExecutor suiteExecutor;
    private final TestWorldManager worldManager;
    private final List<TestRunListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TestRunner(TestDiscoveryEngine discovery, ResultCollector collector) {
        this.discovery = discovery;
        this.collector = collector;
        this.testExecutor = new TestExecutor();
        this.worldManager = new TestWorldManager();
        this.suiteExecutor = new SuiteExecutor(testExecutor, worldManager, listeners);
    }

    public void addListener(TestRunListener listener) {
        listeners.add(listener);
    }

    public List<SuiteResult> run(TestFilter filter) {
        if (!running.compareAndSet(false, true)) {
            LOGGER.atWarning().log("HRTK: A test run is already in progress. Ignoring duplicate request.");
            return List.of();
        }

        try {
            long runStart = System.currentTimeMillis();
            List<SuiteResult> allResults = new ArrayList<>();

            collector.startRun();

            int totalTests = discovery.getRegistry().getTotalTestCount();
            for (TestRunListener listener : listeners) {
                listener.onRunStarted(totalTests);
            }

            try {
                Map<String, List<TestClassInfo>> allTests = discovery.getRegistry().getAllTestClasses();

                for (Map.Entry<String, List<TestClassInfo>> entry : allTests.entrySet()) {
                    String pluginName = entry.getKey();
                    List<TestClassInfo> suites = entry.getValue();

                    for (TestClassInfo suite : suites) {
                        if (!filter.matchesSuite(suite)) continue;

                        boolean anyMatch = suite.getTestMethods().stream()
                                .anyMatch(m -> filter.matchesMethod(suite, m));
                        if (!anyMatch) continue;

                        LOGGER.atInfo().log("HRTK: Running suite '%s' from plugin '%s'",
                                suite.getSuiteName(), pluginName);

                        for (TestRunListener listener : listeners) {
                            listener.onSuiteStarted(pluginName, suite.getSuiteName(), suite.getTestCount());
                        }

                        SuiteResult result = suiteExecutor.execute(suite, filter);
                        allResults.add(result);
                        collector.addResult(result);

                        for (TestRunListener listener : listeners) {
                            listener.onSuiteCompleted(result);
                        }

                        if (filter.isFailFast() && result.countFailed() > 0) {
                            LOGGER.atWarning().log("HRTK: Fail-fast triggered in suite '%s'", suite.getSuiteName());
                            break;
                        }
                    }

                    if (filter.isFailFast() && allResults.stream().anyMatch(r -> r.countFailed() > 0)) {
                        break;
                    }
                }
            } finally {
                worldManager.cleanupAllTestWorlds();
            }

            long totalDuration = System.currentTimeMillis() - runStart;
            collector.finishRun();

            String formatted = ResultFormatter.formatRun(allResults);
            for (String line : formatted.split("\n")) {
                LOGGER.atInfo().log(line);
            }

            long passed = allResults.stream().mapToLong(SuiteResult::countPassed).sum();
            long failed = allResults.stream().mapToLong(SuiteResult::countFailed).sum();
            long skipped = allResults.stream().mapToLong(SuiteResult::countSkipped).sum();
            LOGGER.atInfo().log("HRTK: Run complete - %d passed, %d failed, %d skipped (%dms)",
                    passed, failed, skipped, totalDuration);

            for (TestRunListener listener : listeners) {
                listener.onRunCompleted(allResults, totalDuration);
            }

            return allResults;
        } finally {
            running.set(false);
        }
    }
}
