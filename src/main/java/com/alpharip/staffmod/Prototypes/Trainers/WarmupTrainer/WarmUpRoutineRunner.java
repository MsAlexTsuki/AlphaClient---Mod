package com.alpharip.staffmod.Trainers.WarmupTrainer;

import java.time.LocalDate;

/**
 * Warm-Up Routine Runner.
 * Purpose: Guide a short pre-queue warm-up checklist.
 * Workflow: Timed steps (aim practice, strafing, bridging) with check-offs and a streak of completed warm-ups.
 * Dependencies: Client-side only.
 * Technical direction: Simple timer UI.
 *
 * Prototype based on the original staffops/ideas/WarmUpRoutineRunner.java.
 * Forge event/GUI integration remains a separate step.
 */
public final class WarmUpRoutineRunner {
    public static final String NAME = "Warm-Up Routine Runner";
    public static final String CATEGORY = "Practice";
    public static final String STATUS = "NEW";
    public static final String PURPOSE = "Guide a short pre-queue warm-up checklist.";
    public static final String WORKFLOW = "Timed steps (aim practice, strafing, bridging) with check-offs and a streak of completed warm-ups.";
    public static final String REQUIRED_ACCESS = "All users.";
    public static final String DEPENDENCIES = "Client-side only.";
    public static final String TECHNICAL_DIRECTION = "Simple timer UI.";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;
    // TODO(Supabase): if shared data is later needed, document schema and access policies before integration.
    // TODO(Server): keep sensitive multiplayer actions server-authoritative.

    private final WarmupStats stats;
    private WarmupRoutine selectedRoutine;
    private WarmupSession activeSession;

    public WarmUpRoutineRunner() {
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
