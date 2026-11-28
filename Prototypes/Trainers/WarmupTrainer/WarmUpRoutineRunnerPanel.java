package com.alpharip.staffmod.Trainers.WarmupTrainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** UI-facing snapshot model for WarmUpRoutineRunner; a Minecraft/Forge screen still needs wiring. */
public final class WarmUpRoutineRunnerPanel {
    public static final class StepRow {
        private final String title;
        private final String description;
        private final boolean checked;
        private final boolean current;
        private StepRow(String title, String description, boolean checked, boolean current) {
            this.title = title; this.description = description; this.checked = checked; this.current = current;
        }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public boolean isChecked() { return checked; }
        public boolean isCurrent() { return current; }
    }

    public static final class Snapshot {
        private final String routineName;
        private final String state;
        private final String currentStepTitle;
        private final long remainingMillis;
        private final int completedSteps;
        private final int totalSteps;
        private final int completedRoutines;
        private final int currentStreak;
        private final int bestStreak;
        private final List<StepRow> rows;
        private Snapshot(String routineName, String state, String currentStepTitle, long remainingMillis,
                         int completedSteps, int totalSteps, WarmupStats stats, List<StepRow> rows) {
            this.routineName = routineName; this.state = state; this.currentStepTitle = currentStepTitle;
            this.remainingMillis = remainingMillis; this.completedSteps = completedSteps; this.totalSteps = totalSteps;
            this.completedRoutines = stats.getCompletedRoutines(); this.currentStreak = stats.getCurrentStreak();
            this.bestStreak = stats.getBestStreak(); this.rows = Collections.unmodifiableList(rows);
        }
        public String getRoutineName() { return routineName; }
        public String getState() { return state; }
        public String getCurrentStepTitle() { return currentStepTitle; }
        public long getRemainingMillis() { return remainingMillis; }
        public int getCompletedSteps() { return completedSteps; }
        public int getTotalSteps() { return totalSteps; }
        public int getCompletedRoutines() { return completedRoutines; }
        public int getCurrentStreak() { return currentStreak; }
        public int getBestStreak() { return bestStreak; }
        public List<StepRow> getRows() { return rows; }
    }

    public Snapshot createSnapshot(WarmUpRoutineRunner runner) {
        if (runner == null) throw new IllegalArgumentException("runner must not be null.");
        WarmupRoutine routine = runner.getSelectedRoutine();
        WarmupSession session = runner.getActiveSession();
        List<StepRow> rows = new ArrayList<StepRow>();
        for (int i = 0; i < routine.getSteps().size(); i++) {
            WarmupStep step = routine.getSteps().get(i);
            boolean checked = session != null && session.isStepChecked(i);
            boolean current = session != null && session.getCurrentStepIndex() == i
                    && session.getState() != WarmupSession.State.COMPLETED;
            rows.add(new StepRow(step.getTitle(), step.getDescription(), checked, current));
        }
        WarmupStep current = session == null ? null : session.getCurrentStep();
        return new Snapshot(routine.getName(), session == null ? "NOT_STARTED" : session.getState().name(),
                current == null ? "" : current.getTitle(), session == null ? 0L : session.getRemainingMillis(),
                session == null ? 0 : session.getCompletedStepCount(), routine.getSteps().size(), runner.getStats(), rows);
    }
}
