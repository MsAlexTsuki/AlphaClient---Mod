package com.alpharip.staffmod.Trainers.WarmupTrainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Runtime progress and timer state for one run of a routine. */
public final class WarmupSession {
    public enum State { READY, RUNNING, PAUSED, COMPLETED }

    private final WarmupRoutine routine;
    private final boolean[] checked;
    private int currentStepIndex;
    private State state = State.READY;
    private long stepStartedAt;
    private long elapsedBeforePauseMs;

    public WarmupSession(WarmupRoutine routine) {
        if (routine == null) throw new IllegalArgumentException("routine must not be null.");
        this.routine = routine;
        this.checked = new boolean[routine.getSteps().size()];
    }

    public synchronized void start() {
        if (state != State.READY) throw new IllegalStateException("Only a ready session can be started.");
        state = State.RUNNING;
        stepStartedAt = System.currentTimeMillis();
    }

    public synchronized void pause() {
        if (state != State.RUNNING) return;
        elapsedBeforePauseMs += Math.max(0L, System.currentTimeMillis() - stepStartedAt);
        state = State.PAUSED;
    }

    public synchronized void resume() {
        if (state != State.PAUSED) return;
        stepStartedAt = System.currentTimeMillis();
        state = State.RUNNING;
    }

    public synchronized void markCurrentStepComplete() {
        if (state == State.COMPLETED) return;
        checked[currentStepIndex] = true;
        advance();
    }

    /** Moves forward without marking the current exercise as completed. */
    public synchronized void skipCurrentStep() {
        if (state == State.COMPLETED) return;
        advance();
    }

    private void advance() {
        currentStepIndex++;
        elapsedBeforePauseMs = 0L;
        if (currentStepIndex >= checked.length) {
            state = State.COMPLETED;
        } else {
            state = State.READY;
        }
    }

    public synchronized long getRemainingMillis() {
        if (state == State.COMPLETED) return 0L;
        WarmupStep step = routine.getSteps().get(currentStepIndex);
        long elapsed = elapsedBeforePauseMs;
        if (state == State.RUNNING) elapsed += Math.max(0L, System.currentTimeMillis() - stepStartedAt);
        return Math.max(0L, step.getDurationSeconds() * 1000L - elapsed);
    }

    public synchronized WarmupRoutine getRoutine() { return routine; }
    public synchronized State getState() { return state; }
    public synchronized int getCurrentStepIndex() { return currentStepIndex; }
    public synchronized WarmupStep getCurrentStep() {
        return state == State.COMPLETED ? null : routine.getSteps().get(currentStepIndex);
    }
    public synchronized boolean isStepChecked(int index) { return checked[index]; }
    public synchronized List<Boolean> getChecklist() {
        List<Boolean> result = new ArrayList<Boolean>();
        for (boolean value : checked) result.add(Boolean.valueOf(value));
        return Collections.unmodifiableList(result);
    }
    public synchronized int getCompletedStepCount() {
        int count = 0;
        for (boolean value : checked) if (value) count++;
        return count;
    }
    public synchronized boolean isFullyCompleted() {
        if (state != State.COMPLETED) return false;
        for (boolean value : checked) if (!value) return false;
        return true;
    }
}
