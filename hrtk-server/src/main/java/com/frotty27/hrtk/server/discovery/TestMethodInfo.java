package com.frotty27.hrtk.server.discovery;

import java.lang.reflect.Method;
import java.util.List;

public final class TestMethodInfo {

    private final Method method;
    private final String displayName;
    private final List<String> tags;
    private final int order;
    private final boolean disabled;
    private final String disabledReason;
    private final boolean requiresWorld;
    private final String worldName;
    private final boolean requiresPlayer;
    private final int playerCount;
    private final boolean isEcsTest;
    private final boolean isBenchmark;
    private final int benchmarkWarmup;
    private final int benchmarkIterations;
    private final int benchmarkBatchSize;
    private final boolean isRepeatedTest;
    private final int repeatCount;
    private final boolean isParameterized;
    private final long timeoutMs;
    private final boolean needsTickWaiting;

    public TestMethodInfo(Method method, String displayName, List<String> tags, int order,
                          boolean disabled, String disabledReason,
                          boolean requiresWorld, String worldName,
                          boolean requiresPlayer, int playerCount,
                          boolean isEcsTest, boolean isBenchmark,
                          int benchmarkWarmup, int benchmarkIterations, int benchmarkBatchSize,
                          boolean isRepeatedTest, int repeatCount,
                          boolean isParameterized, long timeoutMs,
                          boolean needsTickWaiting) {
        this.method = method;
        this.displayName = displayName;
        this.tags = tags;
        this.order = order;
        this.disabled = disabled;
        this.disabledReason = disabledReason;
        this.requiresWorld = requiresWorld;
        this.worldName = worldName;
        this.requiresPlayer = requiresPlayer;
        this.playerCount = playerCount;
        this.isEcsTest = isEcsTest;
        this.isBenchmark = isBenchmark;
        this.benchmarkWarmup = benchmarkWarmup;
        this.benchmarkIterations = benchmarkIterations;
        this.benchmarkBatchSize = benchmarkBatchSize;
        this.isRepeatedTest = isRepeatedTest;
        this.repeatCount = repeatCount;
        this.isParameterized = isParameterized;
        this.timeoutMs = timeoutMs;
        this.needsTickWaiting = needsTickWaiting;
    }

    public Method getMethod() {
        return method;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getOrder() {
        return order;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public boolean requiresWorld() {
        return requiresWorld;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean requiresPlayer() {
        return requiresPlayer;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public boolean isEcsTest() {
        return isEcsTest;
    }

    public boolean isBenchmark() {
        return isBenchmark;
    }

    public int getBenchmarkWarmup() {
        return benchmarkWarmup;
    }

    public int getBenchmarkIterations() {
        return benchmarkIterations;
    }

    public int getBenchmarkBatchSize() {
        return benchmarkBatchSize;
    }

    public boolean isRepeatedTest() {
        return isRepeatedTest;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public boolean isParameterized() {
        return isParameterized;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public boolean needsTickWaiting() {
        return needsTickWaiting;
    }

    public boolean matchesTag(String tag) {
        return tags.contains(tag);
    }

    public boolean matchesAnyTag(List<String> candidateTags) {
        for (String tag : candidateTags) {
            if (tags.contains(tag)) return true;
        }
        return false;
    }
}
