package com.frotty27.hrtk.server.ui;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;
import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestDiscoveryEngine;
import com.frotty27.hrtk.server.result.ResultCollector;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.List;
import java.util.Map;

public final class TestDashboardPage extends CustomUIPage {

    private final TestDiscoveryEngine discovery;
    private final ResultCollector collector;

    public TestDashboardPage(PlayerRef playerRef, TestDiscoveryEngine discovery,
                              ResultCollector collector) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.discovery = discovery;
        this.collector = collector;
    }

    @Override
    public void build(Ref<EntityStore> playerRef, UICommandBuilder cmd,
                      UIEventBuilder events, Store<EntityStore> store) {
        cmd.set("title", "HRTK - Hytale Runtime Testing Kit");

        int totalTests = discovery.getTotalTestCount();
        int totalPlugins = discovery.getPluginCount();
        int totalSuites = discovery.getRegistry().getTotalSuiteCount();
        cmd.set("summary", String.format("Discovered: %d test(s) in %d suite(s) across %d plugin(s)",
                totalTests, totalSuites, totalPlugins));

        StringBuilder pluginList = new StringBuilder();
        Map<String, List<TestClassInfo>> all = discovery.getRegistry().getAllTestClasses();
        for (Map.Entry<String, List<TestClassInfo>> entry : all.entrySet()) {
            String pluginName = entry.getKey();
            int count = entry.getValue().stream().mapToInt(TestClassInfo::getTestCount).sum();
            pluginList.append(pluginName).append(" (").append(count).append(" tests)\n");
        }
        cmd.set("plugins", pluginList.toString());

        ResultCollector.RunRecord lastRun = collector.getLastRun();
        if (lastRun != null) {
            cmd.set("lastRunTime", formatTimestamp(lastRun.timestamp()));
            cmd.set("lastRunPassed", String.valueOf(lastRun.countPassed()));
            cmd.set("lastRunFailed", String.valueOf(lastRun.countFailed()));
            cmd.set("lastRunSkipped", String.valueOf(lastRun.countSkipped()));
            cmd.set("lastRunDuration", lastRun.totalDurationMs() + "ms");

            StringBuilder details = new StringBuilder();
            for (SuiteResult suite : lastRun.results()) {
                details.append("\n").append(suite.getSuiteName()).append(":\n");
                for (TestResult test : suite.getResults()) {
                    String status = formatStatus(test.getStatus());
                    details.append("  [").append(status).append("] ")
                            .append(test.getDisplayName())
                            .append(" (").append(test.getDurationMs()).append("ms)");
                    if (test.failed() && test.getMessage() != null) {
                        details.append("\n    ").append(test.getMessage());
                    }
                    details.append('\n');
                }
            }
            cmd.set("results", details.toString());
        } else {
            cmd.set("lastRunTime", "No previous run");
            cmd.set("results", "Use /hrtk run to execute tests");
        }

        cmd.set("instructions", "Commands: /hrtk run | /hrtk list | /hrtk bench | /hrtk scan");
    }

    private String formatStatus(TestStatus status) {
        return switch (status) {
            case PASSED -> "PASS";
            case FAILED -> "FAIL";
            case ERRORED -> "ERR";
            case SKIPPED -> "SKIP";
            case TIMED_OUT -> "TIME";
        };
    }

    private String formatTimestamp(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }
}
