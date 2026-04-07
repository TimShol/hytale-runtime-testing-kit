package com.frotty27.hrtk.server.command;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.frotty27.hrtk.api.lifecycle.TestStatus;
import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestDiscoveryEngine;
import com.frotty27.hrtk.server.result.ResultCollector;
import com.frotty27.hrtk.server.result.ResultExporter;
import com.frotty27.hrtk.server.runner.TestFilter;
import com.frotty27.hrtk.server.runner.TestRunner;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class HRTKCommand extends AbstractCommand {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String COLOR_ACCENT = "#5B9BD5";
    private static final String COLOR_MUTED = "#888888";
    private static final String COLOR_PASS = "#4EC959";
    private static final String COLOR_FAIL = "#E05555";
    private static final String COLOR_SKIP = "#D4A44E";
    private static final String COLOR_ERR = "#C06ADC";
    private static final String COLOR_HEADING = "#BBBBBB";
    private static final String COLOR_CMD = "#8BB8E8";

    private final JavaPlugin plugin;
    private final TestDiscoveryEngine discovery;
    private final TestRunner runner;
    private final ResultCollector collector;

    public HRTKCommand(JavaPlugin plugin, TestDiscoveryEngine discovery,
                       TestRunner runner, ResultCollector collector) {
        super("hrtk", "Hytale Runtime Testing Kit - run and manage tests");
        requirePermission("hrtk.admin");

        this.plugin = plugin;
        this.discovery = discovery;
        this.runner = runner;
        this.collector = collector;

        addSubCommand(new RunSubCommand());
        addSubCommand(new ListSubCommand());
        addSubCommand(new BenchSubCommand());
        addSubCommand(new ResultsSubCommand());
        addSubCommand(new ScanSubCommand());
        addSubCommand(new WatchSubCommand());
        addSubCommand(new ExportSubCommand());
    }

    private final Set<String> watchedPlugins = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public Set<String> getWatchedPlugins() { return watchedPlugins; }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        sendHeader(context, "HRTK");
        send(context, Message.raw(""));
        sendCmdRow(context, "/hrtk run [plugin]", "Run tests and generate report");
        sendCmdRow(context, "/hrtk list [plugin]", "Show all discovered tests");
        sendCmdRow(context, "/hrtk bench [plugin]", "Run benchmarks only");
        sendCmdRow(context, "/hrtk results", "Show last run summary");
        sendCmdRow(context, "/hrtk export", "Export results to JSON + HTML");
        sendCmdRow(context, "/hrtk scan", "Re-discover tests from all plugins");
        sendCmdRow(context, "/hrtk watch [plugin]", "Auto-rerun tests when plugin reloads");

        send(context, Message.raw(""));
        send(context, Message.raw("  Tip: Add ").color(COLOR_MUTED)
                .insert(Message.raw("--help").monospace(true).color(COLOR_CMD))
                .insert(Message.raw(" to any command for details").color(COLOR_MUTED)));

        send(context, Message.raw(""));
        Map<String, List<TestClassInfo>> allClasses = discovery.getRegistry().getAllTestClasses();
        if (!allClasses.isEmpty()) {
            send(context, Message.raw("Plugins with tests:").bold(true).color(COLOR_HEADING));
            for (Map.Entry<String, List<TestClassInfo>> entry : allClasses.entrySet()) {
                int testCount = entry.getValue().stream().mapToInt(TestClassInfo::getTestCount).sum();
                int suiteCount = entry.getValue().size();
                send(context, Message.raw("  ")
                        .insert(Message.raw(entry.getKey()).bold(true).color(COLOR_ACCENT))
                        .insert(Message.raw("  " + testCount + " tests in " + suiteCount + " suites").color(COLOR_MUTED)));
            }
        } else {
            send(context, Message.raw("No plugins with tests found.").color(COLOR_MUTED)
                    .insert(Message.raw(" Run /hrtk scan after loading mods.").color(COLOR_SKIP)));
        }

        return CompletableFuture.completedFuture(null);
    }


    private final class RunSubCommand extends AbstractCommand {
        RunSubCommand() {
            super("run", "Run tests");
            requirePermission("hrtk.admin");
            setAllowsExtraArguments(true);
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext context) {
            if (hasHelpFlag(context)) {
                sendHeader(context, "HRTK Run Help");
                send(context, Message.raw("  What: ").bold(true).color(COLOR_HEADING)
                        .insert(Message.raw("Runs your tests and shows the results.").color(COLOR_MUTED)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                        .insert(Message.raw("/hrtk run [plugin] [--tag tags] [--fail-fast] [--verbose]").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                sendPluginNameHelp(context);
                send(context, Message.raw(""));
                send(context, Message.raw("  Options:").bold(true).color(COLOR_HEADING));
                sendOption(context, "--tag unit", "Only run tests with this tag");
                sendOption(context, "--fail-fast", "Stop as soon as one test fails");
                sendOption(context, "--verbose", "Show each test result as it completes");
                send(context, Message.raw(""));
                send(context, Message.raw("  Examples:").bold(true).color(COLOR_HEADING));
                sendExample(context, "/hrtk run", "Run all tests from all plugins");
                sendExample(context, "/hrtk run Frotty27:MyMod", "Run only MyMod tests");
                sendExample(context, "/hrtk run --tag combat", "Run tests tagged \"combat\"");
                sendExample(context, "/hrtk run Frotty27:MyMod --tag tier", "Run MyMod tier tests only");
                return CompletableFuture.completedFuture(null);
            }

            TestFilter filter = parseFilter(context, false);
            send(context, Message.raw("--- HRTK ---").bold(true).color(COLOR_ACCENT)
                    .insert(Message.raw(" Starting test run...").color(COLOR_MUTED)));

            CompletableFuture.runAsync(() -> {
                try {
                    List<SuiteResult> results = runner.run(filter);

                    long passed = results.stream().mapToLong(SuiteResult::countPassed).sum();
                    long failed = results.stream().mapToLong(SuiteResult::countFailed).sum();
                    long skipped = results.stream().mapToLong(SuiteResult::countSkipped).sum();
                    long duration = results.stream().mapToLong(SuiteResult::getTotalDurationMs).sum();

                    boolean allPassed = failed == 0;
                    String titleColor = allPassed ? COLOR_PASS : COLOR_FAIL;

                    send(context, Message.raw(""));
                    sendHeader(context, "HRTK Test Run Complete", titleColor);

                    Message summary = Message.raw("  ")
                            .insert(Message.raw(passed + " passed").bold(true).color(COLOR_PASS))
                            .insert(Message.raw("  |  ").color(COLOR_MUTED))
                            .insert(Message.raw(failed + " failed").bold(true).color(failed > 0 ? COLOR_FAIL : COLOR_MUTED))
                            .insert(Message.raw("  |  ").color(COLOR_MUTED))
                            .insert(Message.raw(skipped + " skipped").bold(true).color(skipped > 0 ? COLOR_SKIP : COLOR_MUTED));
                    send(context, summary);

                    send(context, Message.raw("  Duration: ").color(COLOR_MUTED)
                            .insert(Message.raw(duration + "ms").color(COLOR_ACCENT)));

                    try {
                        Path exportDir = plugin.getDataDirectory().resolve("results");
                        ResultExporter.exportToJson(exportDir, results);
                        Path htmlFile = ResultExporter.exportToHtml(exportDir, results);
                        send(context, Message.raw("  Report: ").color(COLOR_MUTED)
                                .insert(Message.raw(htmlFile.toString()).monospace(true).color(COLOR_CMD)));
                    } catch (Exception _) {}
                } catch (Exception e) {
                    LOGGER.atSevere().log("HRTK: Test run failed with error: %s", e.getMessage());
                    sendHeader(context, "HRTK Error", COLOR_FAIL);
                    send(context, Message.raw("  Test run failed: ").color(COLOR_FAIL)
                            .insert(Message.raw(e.getMessage()).color(COLOR_MUTED)));
                }
            });
            return CompletableFuture.completedFuture(null);
        }
    }


    private final class ListSubCommand extends AbstractCommand {
        ListSubCommand() {
            super("list", "List discovered tests");
            requirePermission("hrtk.admin");
            setAllowsExtraArguments(true);
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext context) {
            if (hasHelpFlag(context)) {
                sendHeader(context, "HRTK List Help");
                send(context, Message.raw("  What: ").bold(true).color(COLOR_HEADING)
                        .insert(Message.raw("Shows all tests found in your plugins.").color(COLOR_MUTED)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                        .insert(Message.raw("/hrtk list [plugin]").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                sendPluginNameHelp(context);
                send(context, Message.raw(""));
                send(context, Message.raw("  Examples:").bold(true).color(COLOR_HEADING));
                sendExample(context, "/hrtk list", "List all tests from every plugin");
                sendExample(context, "/hrtk list Frotty27:MyMod", "List only MyMod's tests");
                return CompletableFuture.completedFuture(null);
            }

            Map<String, List<TestClassInfo>> all = discovery.getRegistry().getAllTestClasses();
            if (all.isEmpty()) {
                sendHeader(context, "HRTK Test List");
                send(context, Message.raw("  No tests discovered.").color(COLOR_MUTED)
                        .insert(Message.raw(" Ensure mods have @HytaleTest annotations.").color(COLOR_SKIP)));
                return CompletableFuture.completedFuture(null);
            }

            sendHeader(context, "HRTK Test List");
            send(context, Message.raw(""));

            int totalTests = 0;
            int totalSuites = 0;
            int totalPlugins = 0;

            for (Map.Entry<String, List<TestClassInfo>> entry : all.entrySet()) {
                totalPlugins++;
                send(context, Message.raw("  Plugin: ").color(COLOR_MUTED)
                        .insert(Message.raw(entry.getKey()).bold(true).color(COLOR_ACCENT)));

                for (TestClassInfo suite : entry.getValue()) {
                    totalSuites++;
                    int testCount = suite.getTestCount();
                    totalTests += testCount;

                    Message suiteLine = Message.raw("    " + suite.getSuiteName()).color(COLOR_HEADING)
                            .insert(Message.raw(" (" + testCount + " tests)").color(COLOR_MUTED));

                    if (!suite.getClassTags().isEmpty()) {
                        suiteLine = suiteLine.insert(Message.raw(" " + suite.getClassTags()).color(COLOR_MUTED));
                    }
                    if (suite.isClassDisabled()) {
                        suiteLine = suiteLine.insert(Message.raw(" [DISABLED]").color(COLOR_SKIP));
                    }
                    send(context, suiteLine);
                }
                send(context, Message.raw(""));
            }

            send(context, Message.raw("  Total: ").color(COLOR_MUTED)
                    .insert(Message.raw(String.valueOf(totalTests)).bold(true).color(COLOR_ACCENT))
                    .insert(Message.raw(" tests in ").color(COLOR_MUTED))
                    .insert(Message.raw(String.valueOf(totalSuites)).bold(true).color(COLOR_ACCENT))
                    .insert(Message.raw(" suites across ").color(COLOR_MUTED))
                    .insert(Message.raw(String.valueOf(totalPlugins)).bold(true).color(COLOR_ACCENT))
                    .insert(Message.raw(totalPlugins == 1 ? " plugin" : " plugins").color(COLOR_MUTED)));

            return CompletableFuture.completedFuture(null);
        }
    }


    private final class BenchSubCommand extends AbstractCommand {
        BenchSubCommand() {
            super("bench", "Run benchmarks only");
            requirePermission("hrtk.admin");
            setAllowsExtraArguments(true);
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext context) {
            if (hasHelpFlag(context)) {
                sendHeader(context, "HRTK Bench Help");
                send(context, Message.raw("  What: ").bold(true).color(COLOR_HEADING)
                        .insert(Message.raw("Runs only benchmarks and shows performance results.").color(COLOR_MUTED)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                        .insert(Message.raw("/hrtk bench [plugin] [--tag tags]").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                sendPluginNameHelp(context);
                send(context, Message.raw(""));
                send(context, Message.raw("  Options:").bold(true).color(COLOR_HEADING));
                sendOption(context, "--tag perf", "Only run benchmarks with this tag");
                send(context, Message.raw(""));
                send(context, Message.raw("  Examples:").bold(true).color(COLOR_HEADING));
                sendExample(context, "/hrtk bench", "Run all benchmarks from all plugins");
                sendExample(context, "/hrtk bench Frotty27:MyMod", "Run only MyMod benchmarks");
                sendExample(context, "/hrtk bench --tag perf", "Run benchmarks tagged \"perf\"");
                return CompletableFuture.completedFuture(null);
            }

            TestFilter filter = parseFilter(context, true);
            send(context, Message.raw("--- HRTK ---").bold(true).color(COLOR_ACCENT)
                    .insert(Message.raw(" Starting benchmark run...").color(COLOR_MUTED)));

            CompletableFuture.runAsync(() -> {
                try {
                    List<SuiteResult> results = runner.run(filter);

                    long total = results.stream().mapToLong(suite -> suite.getResults().size()).sum();
                    long duration = results.stream().mapToLong(SuiteResult::getTotalDurationMs).sum();

                    send(context, Message.raw(""));
                    sendHeader(context, "HRTK Benchmark Complete");
                    send(context, Message.raw("  ")
                            .insert(Message.raw(total + " benchmark(s)").bold(true).color(COLOR_ACCENT))
                            .insert(Message.raw("  |  ").color(COLOR_MUTED))
                            .insert(Message.raw("Duration: " + duration + "ms").color(COLOR_ACCENT)));
                } catch (Exception e) {
                    LOGGER.atSevere().log("HRTK: Benchmark run failed with error: %s", e.getMessage());
                    sendHeader(context, "HRTK Error", COLOR_FAIL);
                    send(context, Message.raw("  Benchmark run failed: ").color(COLOR_FAIL)
                            .insert(Message.raw(e.getMessage()).color(COLOR_MUTED)));
                }
            });
            return CompletableFuture.completedFuture(null);
        }
    }


    private final class ResultsSubCommand extends AbstractCommand {
        ResultsSubCommand() {
            super("results", "Show last run results");
            requirePermission("hrtk.admin");
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext context) {
            if (hasHelpFlag(context)) {
                sendHeader(context, "HRTK Results Help");
                send(context, Message.raw("  What: ").bold(true).color(COLOR_HEADING)
                        .insert(Message.raw("Shows results from the last time you ran tests.").color(COLOR_MUTED)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                        .insert(Message.raw("/hrtk results").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Examples:").bold(true).color(COLOR_HEADING));
                sendExample(context, "/hrtk results", "Show results from the last test run");
                return CompletableFuture.completedFuture(null);
            }

            ResultCollector.RunRecord lastRun = collector.getLastRun();
            if (lastRun == null) {
                sendHeader(context, "HRTK Last Run Results");
                send(context, Message.raw("  No previous run results.").color(COLOR_MUTED)
                        .insert(Message.raw(" Use /hrtk run first.").color(COLOR_SKIP)));
                return CompletableFuture.completedFuture(null);
            }

            sendHeader(context, "HRTK Last Run Results");

            long totalPassed = 0;
            long totalFailed = 0;
            long totalSkipped = 0;
            long totalDuration = 0;

            for (SuiteResult suite : lastRun.results()) {
                send(context, Message.raw(suite.getSuiteName()).bold(true).color(COLOR_HEADING)
                        .insert(Message.raw(" (" + suite.getPluginName() + ")").color(COLOR_MUTED)));

                for (TestResult test : suite.getResults()) {
                    Message statusBadge = statusBadge(test.getStatus());
                    Message line = Message.raw("  ")
                            .insert(statusBadge)
                            .insert(Message.raw(" " + test.getTestName()))
                            .insert(Message.raw(" (" + test.getDurationMs() + "ms)").color(COLOR_MUTED));
                    send(context, line);

                    if (test.getMessage() != null && test.failed()) {
                        send(context, Message.raw("       " + test.getMessage()).color(COLOR_FAIL));
                    }
                }

                totalPassed += suite.countPassed();
                totalFailed += suite.countFailed();
                totalSkipped += suite.countSkipped();
                totalDuration += suite.getTotalDurationMs();
            }

            send(context, Message.raw(""));
            send(context, Message.raw(totalPassed + " passed").color(COLOR_PASS)
                    .insert(Message.raw(", ").color(COLOR_MUTED))
                    .insert(Message.raw(totalFailed + " failed").color(totalFailed > 0 ? COLOR_FAIL : COLOR_MUTED))
                    .insert(Message.raw(", ").color(COLOR_MUTED))
                    .insert(Message.raw(totalSkipped + " skipped").color(totalSkipped > 0 ? COLOR_SKIP : COLOR_MUTED))
                    .insert(Message.raw(" (" + totalDuration + "ms)").color(COLOR_MUTED)));

            return CompletableFuture.completedFuture(null);
        }
    }


    private final class ScanSubCommand extends AbstractCommand {
        ScanSubCommand() {
            super("scan", "Re-scan plugins for tests");
            requirePermission("hrtk.admin");
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext context) {
            if (hasHelpFlag(context)) {
                sendHeader(context, "HRTK Scan Help");
                send(context, Message.raw("  What: ").bold(true).color(COLOR_HEADING)
                        .insert(Message.raw("Looks through all loaded plugins for new or changed tests.").color(COLOR_MUTED)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                        .insert(Message.raw("/hrtk scan").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Examples:").bold(true).color(COLOR_HEADING));
                sendExample(context, "/hrtk scan", "Find all tests in all loaded plugins");
                return CompletableFuture.completedFuture(null);
            }

            send(context, Message.raw("--- HRTK ---").bold(true).color(COLOR_ACCENT)
                    .insert(Message.raw(" Scanning plugins...").color(COLOR_MUTED)));

            discovery.scanAll(plugin);

            sendHeader(context, "HRTK Scan Complete");
            send(context, Message.raw("  Scanned all loaded plugins").color(COLOR_MUTED));
            send(context, Message.raw(""));

            Map<String, List<TestClassInfo>> scanned = discovery.getRegistry().getAllTestClasses();
            int totalScannedTests = 0;
            for (Map.Entry<String, List<TestClassInfo>> entry : scanned.entrySet()) {
                int testCount = entry.getValue().stream().mapToInt(TestClassInfo::getTestCount).sum();
                int suiteCount = entry.getValue().size();
                totalScannedTests += testCount;
                send(context, Message.raw("  ")
                        .insert(Message.raw(entry.getKey()).bold(true).color(COLOR_ACCENT))
                        .insert(Message.raw("  " + testCount + " tests in " + suiteCount + " suites").color(COLOR_MUTED)));
            }

            send(context, Message.raw(""));
            send(context, Message.raw("  Total: ").color(COLOR_MUTED)
                    .insert(Message.raw(String.valueOf(totalScannedTests)).bold(true).color(COLOR_PASS))
                    .insert(Message.raw(" tests across ").color(COLOR_MUTED))
                    .insert(Message.raw(String.valueOf(discovery.getPluginCount())).bold(true).color(COLOR_ACCENT))
                    .insert(Message.raw(discovery.getPluginCount() == 1 ? " plugin" : " plugins").color(COLOR_MUTED)));

            return CompletableFuture.completedFuture(null);
        }
    }


    private final class WatchSubCommand extends AbstractCommand {
        WatchSubCommand() {
            super("watch", "Re-run tests when a plugin reloads");
            requirePermission("hrtk.admin");
            setAllowsExtraArguments(true);
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext context) {
            if (hasHelpFlag(context)) {
                sendHeader(context, "HRTK Watch Help");
                send(context, Message.raw("  What: ").bold(true).color(COLOR_HEADING)
                        .insert(Message.raw("Automatically re-runs tests when a plugin reloads.").color(COLOR_MUTED)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                        .insert(Message.raw("/hrtk watch [plugin]").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                sendPluginNameHelp(context);
                send(context, Message.raw(""));
                send(context, Message.raw("  Examples:").bold(true).color(COLOR_HEADING));
                sendExample(context, "/hrtk watch", "Show which plugins are being watched");
                sendExample(context, "/hrtk watch Frotty27:MyMod", "Start or stop watching MyMod");
                return CompletableFuture.completedFuture(null);
            }

            String input = context.getInputString();
            String[] parts = input.trim().split("\\s+");

            String targetPlugin = null;
            for (int i = 0; i < parts.length; i++) {
                if (!"hrtk".equalsIgnoreCase(parts[i]) && !"watch".equalsIgnoreCase(parts[i])
                        && !parts[i].startsWith("-")) {
                    targetPlugin = parts[i];
                    break;
                }
            }

            if (targetPlugin == null) {
                sendHeader(context, "HRTK Watch");
                if (watchedPlugins.isEmpty()) {
                    send(context, Message.raw("  No plugins being watched.").color(COLOR_MUTED));
                    send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                            .insert(Message.raw("/hrtk watch <plugin>").monospace(true).color(COLOR_CMD)));
                } else {
                    send(context, Message.raw("  Watching:").color(COLOR_HEADING));
                    for (String watched : watchedPlugins) {
                        send(context, Message.raw("    - ").color(COLOR_MUTED)
                                .insert(Message.raw(watched).color(COLOR_PASS)));
                    }
                    send(context, Message.raw(""));
                    send(context, Message.raw("  Use ").color(COLOR_MUTED)
                            .insert(Message.raw("/hrtk watch <plugin>").monospace(true).color(COLOR_CMD))
                            .insert(Message.raw(" to toggle.").color(COLOR_MUTED)));
                }
                return CompletableFuture.completedFuture(null);
            }

            if (watchedPlugins.contains(targetPlugin)) {
                watchedPlugins.remove(targetPlugin);
                sendHeader(context, "HRTK Watch");
                send(context, Message.raw("  Stopped watching ").color(COLOR_MUTED)
                        .insert(Message.raw(targetPlugin).bold(true).color(COLOR_SKIP)));
            } else {
                watchedPlugins.add(targetPlugin);
                sendHeader(context, "HRTK Watch");
                send(context, Message.raw("  Now watching ").color(COLOR_MUTED)
                        .insert(Message.raw(targetPlugin).bold(true).color(COLOR_PASS))
                        .insert(Message.raw(" - tests will re-run on reload").color(COLOR_MUTED)));
            }
            return CompletableFuture.completedFuture(null);
        }
    }


    private final class ExportSubCommand extends AbstractCommand {
        ExportSubCommand() {
            super("export", "Export last run results to JSON + HTML");
            requirePermission("hrtk.admin");
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext context) {
            if (hasHelpFlag(context)) {
                sendHeader(context, "HRTK Export Help");
                send(context, Message.raw("  What: ").bold(true).color(COLOR_HEADING)
                        .insert(Message.raw("Saves the last test results as JSON and HTML files.").color(COLOR_MUTED)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Usage: ").color(COLOR_MUTED)
                        .insert(Message.raw("/hrtk export").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Files are saved to the plugin data directory under ").color(COLOR_MUTED)
                        .insert(Message.raw("results/").monospace(true).color(COLOR_CMD)));
                send(context, Message.raw(""));
                send(context, Message.raw("  Examples:").bold(true).color(COLOR_HEADING));
                sendExample(context, "/hrtk export", "Save last results as JSON + HTML files");
                return CompletableFuture.completedFuture(null);
            }

            ResultCollector.RunRecord lastRun = collector.getLastRun();
            if (lastRun == null) {
                sendHeader(context, "HRTK Export");
                send(context, Message.raw("  No results to export.").color(COLOR_MUTED)
                        .insert(Message.raw(" Run tests first.").color(COLOR_SKIP)));
                return CompletableFuture.completedFuture(null);
            }

            try {
                Path exportDir = plugin.getDataDirectory().resolve("results");
                Path jsonFile = ResultExporter.exportToJson(exportDir, lastRun.results());
                Path htmlFile = ResultExporter.exportToHtml(exportDir, lastRun.results());
                sendHeader(context, "HRTK Export Complete");
                send(context, Message.raw("  JSON: ").color(COLOR_MUTED)
                        .insert(Message.raw(jsonFile.toString()).monospace(true).color(COLOR_PASS)));
                send(context, Message.raw("  HTML: ").color(COLOR_MUTED)
                        .insert(Message.raw(htmlFile.toString()).monospace(true).color(COLOR_PASS)));
            } catch (Exception e) {
                sendHeader(context, "HRTK Error", COLOR_FAIL);
                send(context, Message.raw("  Export failed: ").color(COLOR_FAIL)
                        .insert(Message.raw(e.getMessage()).color(COLOR_MUTED)));
            }
            return CompletableFuture.completedFuture(null);
        }
    }


    private static void send(CommandContext context, Message message) {
        context.sendMessage(message);
    }

    private static void sendHeader(CommandContext context, String title) {
        sendHeader(context, title, COLOR_ACCENT);
    }

    private static void sendHeader(CommandContext context, String title, String color) {
        send(context, Message.raw("--- " + title + " ---").bold(true).color(color));
    }

    private static void sendCmdRow(CommandContext context, String command, String description) {
        send(context, Message.raw("  ")
                .insert(Message.raw(command).monospace(true).color(COLOR_CMD))
                .insert(Message.raw("  " + description).color(COLOR_MUTED)));
    }

    private static void sendOption(CommandContext context, String flag, String description) {
        send(context, Message.raw("    ")
                .insert(Message.raw(flag).monospace(true).color(COLOR_ACCENT))
                .insert(Message.raw("  " + description).color(COLOR_MUTED)));
    }

    private static void sendExample(CommandContext context, String example) {
        send(context, Message.raw("    ")
                .insert(Message.raw(example).monospace(true).color(COLOR_CMD)));
    }

    private static void sendExample(CommandContext context, String example, String description) {
        send(context, Message.raw("    ")
                .insert(Message.raw(example).monospace(true).color(COLOR_CMD))
                .insert(Message.raw("  " + description).color(COLOR_MUTED)));
    }

    private static void sendPluginNameHelp(CommandContext context) {
        send(context, Message.raw("  Plugin name:").bold(true).color(COLOR_HEADING));
        send(context, Message.raw("    Use the name shown in ").color(COLOR_MUTED)
                .insert(Message.raw("/hrtk").monospace(true).color(COLOR_CMD))
                .insert(Message.raw(" under \"Plugins with tests\"").color(COLOR_MUTED)));
        send(context, Message.raw("    If the name has spaces, wrap it in quotes").color(COLOR_MUTED));
    }

    private static Message statusBadge(TestStatus status) {
        return switch (status) {
            case PASSED -> Message.raw("[PASS]").bold(true).color(COLOR_PASS);
            case FAILED -> Message.raw("[FAIL]").bold(true).color(COLOR_FAIL);
            case ERRORED -> Message.raw("[ERR]").bold(true).color(COLOR_ERR);
            case SKIPPED -> Message.raw("[SKIP]").bold(true).color(COLOR_SKIP);
            case TIMED_OUT -> Message.raw("[TIME]").bold(true).color(COLOR_FAIL);
        };
    }

    private static boolean hasHelpFlag(CommandContext context) {
        String input = context.getInputString();
        if (input == null) return false;
        String[] parts = input.trim().split("\\s+");
        for (String part : parts) {
            if ("--help".equalsIgnoreCase(part) || "-h".equalsIgnoreCase(part)) {
                return true;
            }
        }
        return false;
    }

    private TestFilter parseFilter(CommandContext context, boolean benchmarkOnly) {
        TestFilter.Builder builder = TestFilter.builder()
                .benchmarkOnly(benchmarkOnly);

        try {
            String input = context.getInputString();
            String[] parts = input.trim().split("\\s+");
            List<String> tags = new ArrayList<>();

            int start = 0;
            for (int i = 0; i < parts.length && i < 2; i++) {
                if ("hrtk".equalsIgnoreCase(parts[i]) || "run".equalsIgnoreCase(parts[i])
                        || "bench".equalsIgnoreCase(parts[i])) {
                    start = i + 1;
                }
            }

            for (int i = start; i < parts.length; i++) {
                switch (parts[i]) {
                    case "--tag" -> {
                        if (i + 1 < parts.length) {
                            for (String tag : parts[++i].split(",")) {
                                tags.add(tag.trim());
                            }
                        }
                    }
                    case "--fail-fast" -> builder.failFast(true);
                    case "--verbose" -> builder.verbose(true);
                    default -> {
                        String arg = parts[i];
                        if (!arg.startsWith("-")) {
                            if (arg.startsWith("\"")) {
                                StringBuilder quoted = new StringBuilder(arg.substring(1));
                                while (i + 1 < parts.length && !parts[i].endsWith("\"")) {
                                    i++;
                                    quoted.append(" ").append(parts[i]);
                                }
                                String quotedStr = quoted.toString();
                                if (quotedStr.endsWith("\"")) {
                                    quotedStr = quotedStr.substring(0, quotedStr.length() - 1);
                                }
                                builder.plugin(quotedStr);
                            } else if (arg.contains(".")) {
                                String[] pluginParts = arg.split("\\.");
                                builder.plugin(pluginParts[0]);
                                if (pluginParts.length > 1) {
                                    String suiteAndMethod = pluginParts[1];
                                    if (suiteAndMethod.contains("#")) {
                                        String[] suiteParts = suiteAndMethod.split("#");
                                        builder.suite(suiteParts[0]);
                                        builder.method(suiteParts[1]);
                                    } else {
                                        builder.suite(suiteAndMethod);
                                    }
                                }
                            } else {
                                builder.plugin(arg);
                            }
                        }
                    }
                }
            }

            if (!tags.isEmpty()) builder.tags(tags);
        } catch (Exception _) {
        }

        return builder.build();
    }
}
