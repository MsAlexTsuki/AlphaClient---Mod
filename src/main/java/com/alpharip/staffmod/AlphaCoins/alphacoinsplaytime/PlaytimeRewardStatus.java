package com.alpharip.staffmod.AlphaCoins.alphacoinsplaytime;

/** Immutable progress information for one recurring reward or milestone. */
public final class PlaytimeRewardStatus {
    private final PlaytimeRewardDefinition definition;
    private final long progressMillis;
    private final long remainingMillis;
    private final long completedIntervals;
    private final boolean earned;

    public PlaytimeRewardStatus(PlaytimeRewardDefinition definition, long progressMillis,
                                long remainingMillis, long completedIntervals, boolean earned) {
        if (definition == null) throw new IllegalArgumentException("Reward definition is required.");
        this.definition = definition;
        this.progressMillis = Math.max(0L, progressMillis);
        this.remainingMillis = Math.max(0L, remainingMillis);
        this.completedIntervals = Math.max(0L, completedIntervals);
        this.earned = earned;
    }

    public PlaytimeRewardDefinition getDefinition() { return definition; }
    public long getProgressMillis() { return progressMillis; }
    public long getRemainingMillis() { return remainingMillis; }
    public long getCompletedIntervals() { return completedIntervals; }
    public boolean isEarned() { return earned; }

    public int getProgressPercent() {
        long required = definition.getThresholdMillis();
        if (required <= 0L) return 0;
        return (int) Math.min(100L, (progressMillis * 100L) / required);
    }
}
