package com.alpharip.staffmod.badges.playtime;

/** Cumulative Minecraft runtime milestones for playtime badges. */
public enum PlaytimeBadgeTier {
    ONE_HOUR("1 Hour", 60L * 60L),
    ONE_DAY("1 Day", 24L * 60L * 60L),
    ONE_MONTH("1 Month", 30L * 24L * 60L * 60L),
    THREE_MONTHS("3 Months", 3L * 30L * 24L * 60L * 60L),
    SIX_MONTHS("6 Months", 6L * 30L * 24L * 60L * 60L),
    ONE_YEAR("1 Year", 365L * 24L * 60L * 60L),
    TWO_YEARS("2 Years", 2L * 365L * 24L * 60L * 60L);

    private final String displayName;
    private final long requiredSeconds;

    PlaytimeBadgeTier(String displayName, long requiredSeconds) {
        this.displayName = displayName;
        this.requiredSeconds = requiredSeconds;
    }

    public String getDisplayName() { return displayName; }
    public long getRequiredSeconds() { return requiredSeconds; }
}