package com.frotty27.hrtk.server.runner;

import com.frotty27.hrtk.server.discovery.TestClassInfo;
import com.frotty27.hrtk.server.discovery.TestMethodInfo;

import java.util.ArrayList;
import java.util.List;

public final class TestFilter {

    private final String pluginName;
    private final List<String> tags;
    private final String suiteName;
    private final String methodName;
    private final boolean benchmarkOnly;
    private final boolean failFast;
    private final boolean verbose;

    private TestFilter(String pluginName, List<String> tags, String suiteName,
                       String methodName, boolean benchmarkOnly, boolean failFast, boolean verbose) {
        this.pluginName = pluginName;
        this.tags = tags;
        this.suiteName = suiteName;
        this.methodName = methodName;
        this.benchmarkOnly = benchmarkOnly;
        this.failFast = failFast;
        this.verbose = verbose;
    }

    public static TestFilter all() {
        return new TestFilter(null, List.of(), null, null, false, false, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean matchesSuite(TestClassInfo suite) {
        if (pluginName != null && !pluginName.equalsIgnoreCase(suite.getPluginName())) {
            return false;
        }
        if (suiteName != null && !suiteName.equalsIgnoreCase(suite.getSuiteName())) {
            return false;
        }
        return true;
    }

    public boolean matchesMethod(TestClassInfo suite, TestMethodInfo method) {
        if (!matchesSuite(suite)) return false;
        if (methodName != null && !methodName.equals(method.getMethod().getName())) {
            return false;
        }
        if (benchmarkOnly && !method.isBenchmark()) {
            return false;
        }
        if (!tags.isEmpty()) {
            boolean hasTag = suite.getClassTags().stream().anyMatch(tags::contains);
            if (!hasTag) {
                hasTag = method.getTags().stream().anyMatch(tags::contains);
            }
            if (!hasTag) return false;
        }
        return true;
    }

    public String getPluginName() { return pluginName; }
    public List<String> getTags() { return tags; }
    public String getSuiteName() { return suiteName; }
    public String getMethodName() { return methodName; }
    public boolean isBenchmarkOnly() { return benchmarkOnly; }
    public boolean isFailFast() { return failFast; }
    public boolean isVerbose() { return verbose; }

    public static final class Builder {

        private String pluginName;
        private List<String> tags = new ArrayList<>();
        private String suiteName;
        private String methodName;
        private boolean benchmarkOnly;
        private boolean failFast;
        private boolean verbose;

        private Builder() {}

        public Builder plugin(String pluginName) {
            this.pluginName = pluginName;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder suite(String suiteName) {
            this.suiteName = suiteName;
            return this;
        }

        public Builder method(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder benchmarkOnly(boolean benchmarkOnly) {
            this.benchmarkOnly = benchmarkOnly;
            return this;
        }

        public Builder failFast(boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public TestFilter build() {
            return new TestFilter(pluginName, List.copyOf(tags), suiteName,
                    methodName, benchmarkOnly, failFast, verbose);
        }
    }
}
