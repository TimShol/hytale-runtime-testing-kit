package com.frotty27.hrtk.server.discovery;

import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

import java.lang.reflect.Method;
import java.util.List;

public final class TestClassInfo {

    private final Class<?> testClass;
    private final String suiteName;
    private final String pluginName;
    private final IsolationStrategy isolation;
    private final List<String> classTags;
    private final boolean classDisabled;
    private final String classDisabledReason;
    private final List<TestMethodInfo> testMethods;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;
    private final List<Method> beforeEachMethods;
    private final List<Method> afterEachMethods;

    public TestClassInfo(Class<?> testClass, String suiteName, String pluginName,
                         IsolationStrategy isolation, List<String> classTags,
                         boolean classDisabled, String classDisabledReason,
                         List<TestMethodInfo> testMethods,
                         List<Method> beforeAllMethods, List<Method> afterAllMethods,
                         List<Method> beforeEachMethods, List<Method> afterEachMethods) {
        this.testClass = testClass;
        this.suiteName = suiteName;
        this.pluginName = pluginName;
        this.isolation = isolation;
        this.classTags = classTags;
        this.classDisabled = classDisabled;
        this.classDisabledReason = classDisabledReason;
        this.testMethods = testMethods;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
        this.beforeEachMethods = beforeEachMethods;
        this.afterEachMethods = afterEachMethods;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public IsolationStrategy getIsolation() {
        return isolation;
    }

    public List<String> getClassTags() {
        return classTags;
    }

    public boolean isClassDisabled() {
        return classDisabled;
    }

    public String getClassDisabledReason() {
        return classDisabledReason;
    }

    public List<TestMethodInfo> getTestMethods() {
        return testMethods;
    }

    public List<Method> getBeforeAllMethods() {
        return beforeAllMethods;
    }

    public List<Method> getAfterAllMethods() {
        return afterAllMethods;
    }

    public List<Method> getBeforeEachMethods() {
        return beforeEachMethods;
    }

    public List<Method> getAfterEachMethods() {
        return afterEachMethods;
    }

    public int getTestCount() {
        return testMethods.size();
    }
}
