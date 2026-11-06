package com.alpharip.staffmod.badges.playtime;

/** Snapshot of one playtime badge against the current cumulative playtime. */
public final class PlaytimeBadgeStatus {
    private final PlaytimeBadgeTier tier;
    private final long currentSeconds;

    public PlaytimeBadgeStatus(PlaytimeBadgeTier tier, long currentSeconds) {
        if (tier == null) throw new IllegalArgumentException("Badge tier is required.");
        this.tier = tier;
        this.currentSeconds = Math.max(0L, currentSeconds);
    }

    public PlaytimeBadgeTier getTier() { return tier; }
    public long getCurrentSeconds() { return currentSeconds; }
    public long getRequiredSeconds() { return tier.getRequiredSeconds(); }
    public boolean isEarned() { return currentSeconds >= tier.getRequiredSeconds(); }
    public long getRemainingSeconds() {
        return Math.max(0L, tier.getRequiredSeconds() - currentSeconds);
    }
    public int getProgressPercent() {
        if (isEarned()) return 100;
        return (int) Math.min(99L, currentSeconds * 100L / tier.getRequiredSeconds());
    }
}