package com.alpharip.staffmod.Trainers.WarmupTrainer;

import java.time.LocalDate;

/** Entry point coordinating routine selection, sessions, and completion stats. */
public final class WarmupTrainer {
    public static final String NAME = "Warmup Trainer";
    public static final String CATEGORY = "Trainers";
    public static final String REQUIRED_ACCESS = "All users.";
    public static final String DEPENDENCIES = "Client-side only.";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;
    // TODO(Supabase): keep this local-only unless shared statistics are explicitly approved.

    private final WarmupStats stats;
    private WarmupRoutine selectedRoutine;
    private WarmupSession activeSession;

    public WarmupTrainer() {
        this.stats = new WarmupStats();
        this.selectedRoutine = WarmupRoutine.createDefault();
    }

    public synchronized void selectRoutine(WarmupRoutine routine) {
        if (routine == null) throw new IllegalArgumentException("routine must not be null.");
        if (activeSession != null && activeSession.getState() != WarmupSession.State.COMPLETED)
            throw new IllegalStateException("Finish or discard the active session before changing routines.");
        selectedRoutine = routine;
        activeSession = null;
    }

    public synchronized WarmupSession begin() {
        activeSession = new WarmupSession(selectedRoutine);
        return activeSession;
    }

    public synchronized boolean recordIfCompleted() {
        if (activeSession == null || !activeSession.isFullyCompleted()) return false;
        stats.recordCompletion(LocalDate.now().toString());
        return true;
    }

    public synchronized WarmupRoutine getSelectedRoutine() { return selectedRoutine; }
    public synchronized WarmupSession getActiveSession() { return activeSession; }
    public WarmupStats getStats() { return stats; }
}
