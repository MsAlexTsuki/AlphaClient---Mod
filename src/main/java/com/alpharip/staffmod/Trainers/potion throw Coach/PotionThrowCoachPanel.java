package com.alpharip.staffmod.trainers.potionthrowcoach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UI-facing view model for a future in-client Potion Throw Coach screen.
 * It exposes display-ready values but deliberately has no Minecraft GUI hooks.
 */
public final class PotionThrowCoachPanel {
    private final PotionThrowCoach coach;

    public PotionThrowCoachPanel(PotionThrowCoach coach) {
        if (coach == null) throw new IllegalArgumentException("coach must not be null.");
        this.coach = coach;
    }

    public Snapshot createSnapshot() {
        PotionThrowCoachStats stats = coach.getStats();
        List<String> recentRows = new ArrayList<String>();
        List<PotionThrowResult> results = stats.getResults();
        for (int i = results.size() - 1; i >= 0 && recentRows.size() < 8; i--) {
            PotionThrowResult result = results.get(i);
            String reaction = result.getReactionTimeMillis() < 0L
                    ? "N/A" : result.getReactionTimeMillis() + " ms";
            recentRows.add("#" + result.getId()
                    + " | reaction " + reaction
                    + " | landing offset " + format(result.getLandingOffset()) + " blocks"
                    + " | " + (result.isAccurate() ? "Accurate" : "Miss"));
        }
        return new Snapshot(
                stats.getCompletedThrows(),
                format(stats.getAccuracyPercent()) + "%",
                format(stats.getAverageReactionTimeMillis()) + " ms",
                format(stats.getAverageLandingOffset()) + " blocks",
                coach.hasPendingThrow(),
                recentRows
        );
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    public static final class Snapshot {
        private final int completedThrows;
        private final String accuracy;
        private final String averageReactionTime;
        private final String averageLandingOffset;
        private final boolean awaitingLanding;
        private final List<String> recentRows;

        private Snapshot(int completedThrows, String accuracy,
                         String averageReactionTime, String averageLandingOffset,
                         boolean awaitingLanding, List<String> recentRows) {
            this.completedThrows = completedThrows;
            this.accuracy = accuracy;
            this.averageReactionTime = averageReactionTime;
            this.averageLandingOffset = averageLandingOffset;
            this.awaitingLanding = awaitingLanding;
            this.recentRows = Collections.unmodifiableList(
                    new ArrayList<String>(recentRows));
        }

        public int getCompletedThrows() { return completedThrows; }
        public String getAccuracy() { return accuracy; }
        public String getAverageReactionTime() { return averageReactionTime; }
        public String getAverageLandingOffset() { return averageLandingOffset; }
        public boolean isAwaitingLanding() { return awaitingLanding; }
        public List<String> getRecentRows() { return recentRows; }
    }
}
