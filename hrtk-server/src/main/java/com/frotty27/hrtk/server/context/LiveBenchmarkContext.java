package com.frotty27.hrtk.server.context;

import com.frotty27.hrtk.api.context.BenchmarkContext;
import com.frotty27.hrtk.api.mock.EventCapture;
import com.frotty27.hrtk.api.mock.MockCommandSender;
import com.hypixel.hytale.common.benchmark.TimeRecorder;

public final class LiveBenchmarkContext extends LiveTestContext implements BenchmarkContext {

    private final TimeRecorder timeRecorder;
    private final int totalIterations;
    private int currentIteration;
    private boolean warmup;
    private long manualStartNanos;
    private boolean manualTiming;

    public LiveBenchmarkContext(String pluginName, int totalIterations) {
        super(pluginName);
        this.timeRecorder = new TimeRecorder();
        this.totalIterations = totalIterations;
    }

    @Override
    public int getIteration() {
        return currentIteration;
    }

    @Override
    public int getTotalIterations() {
        return totalIterations;
    }

    @Override
    public boolean isWarmup() {
        return warmup;
    }

    @Override
    public void startTimer() {
        manualTiming = true;
        manualStartNanos = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        if (manualTiming && !warmup) {
            long elapsed = System.nanoTime() - manualStartNanos;
            timeRecorder.recordNanos(elapsed);
            manualTiming = false;
        }
    }


    public void setIteration(int iteration) {
        this.currentIteration = iteration;
    }

    public void setWarmup(boolean warmup) {
        this.warmup = warmup;
    }

    public boolean isManualTiming() {
        return manualTiming;
    }

    public void endAutoTimer(long startNanos) {
        timeRecorder.end(startNanos);
    }

    public void resetManualTiming() {
        manualTiming = false;
    }

    public TimeRecorder getTimeRecorder() {
        return timeRecorder;
    }
}
