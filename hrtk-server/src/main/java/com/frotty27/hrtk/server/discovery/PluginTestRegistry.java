package com.frotty27.hrtk.server.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PluginTestRegistry {

    private final Map<String, List<TestClassInfo>> registry = new java.util.concurrent.ConcurrentHashMap<>();

    public void register(String pluginName, TestClassInfo testClassInfo) {
        registry.computeIfAbsent(pluginName, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(testClassInfo);
    }

    public void clearPlugin(String pluginName) {
        registry.remove(pluginName);
    }

    public void clear() {
        registry.clear();
    }

    public List<TestClassInfo> getTestClasses(String pluginName) {
        return registry.getOrDefault(pluginName, Collections.emptyList());
    }

    public Map<String, List<TestClassInfo>> getAllTestClasses() {
        return Collections.unmodifiableMap(registry);
    }

    public List<String> getPluginNames() {
        return new ArrayList<>(registry.keySet());
    }

    public int getPluginCount() {
        return registry.size();
    }

    public int getTotalTestCount() {
        int count = 0;
        for (List<TestClassInfo> classes : registry.values()) {
            for (TestClassInfo info : classes) {
                count += info.getTestCount();
            }
        }
        return count;
    }

    public int getTotalSuiteCount() {
        int count = 0;
        for (List<TestClassInfo> classes : registry.values()) {
            count += classes.size();
        }
        return count;
    }
}
