package com.frotty27.hrtk.server;

import com.frotty27.hrtk.server.command.HRTKCommand;
import com.frotty27.hrtk.server.discovery.TestDiscoveryEngine;
import com.frotty27.hrtk.server.result.ResultCollector;
import com.frotty27.hrtk.server.runner.TestFilter;
import com.frotty27.hrtk.server.runner.TestRunner;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;

public final class HRTKPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private TestDiscoveryEngine discoveryEngine;
    private ResultCollector resultCollector;
    private TestRunner testRunner;
    private HRTKCommand hrtkCommand;

    public HRTKPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        discoveryEngine = new TestDiscoveryEngine();
        resultCollector = new ResultCollector();
        testRunner = new TestRunner(discoveryEngine, resultCollector);

        hrtkCommand = new HRTKCommand(this, discoveryEngine, testRunner, resultCollector);
        getCommandRegistry().registerCommand(hrtkCommand);

        getEventRegistry().registerGlobal(PluginSetupEvent.class, event -> {
            PluginBase plugin = event.getPlugin();
            if (plugin instanceof JavaPlugin javaPlugin && plugin != this) {
                discoveryEngine.scanPlugin(javaPlugin);

                String pluginName = plugin.getName();
                if (hrtkCommand.getWatchedPlugins().contains(pluginName)) {
                    LOGGER.atInfo().log("HRTK: Watched plugin '%s' reloaded - re-running tests", pluginName);
                    TestFilter filter = TestFilter.builder().plugin(pluginName).build();
                    testRunner.run(filter);
                }
            }
        });

        LOGGER.atInfo().log("HRTK: Setup complete. Awaiting server start to scan plugins.");
    }

    @Override
    protected void start() {
        discoveryEngine.scanAll(this);

        int tests = discoveryEngine.getTotalTestCount();
        int plugins = discoveryEngine.getPluginCount();

        if (tests > 0) {
            LOGGER.atInfo().log("HRTK: Discovered %d test(s) in %d suite(s) across %d plugin(s). Use /hrtk run to execute.",
                    tests, discoveryEngine.getRegistry().getTotalSuiteCount(), plugins);
        } else {
            LOGGER.atInfo().log("HRTK: No tests discovered. Add @HytaleTest annotations to your mod classes.");
        }
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("HRTK: Shutting down.");
    }

    public TestDiscoveryEngine getDiscoveryEngine() { return discoveryEngine; }
    public ResultCollector getResultCollector() { return resultCollector; }
    public TestRunner getTestRunner() { return testRunner; }
}
