package com.alpharip.staffmod.staffops.ideas;

/**
 * Snapshot model for statistics the client already knows about.
 * Server-only lifetime, punishment, or account statistics are intentionally not fabricated.
 */
public final class PlayerStatisticsViewer {
    private String playerName = "";
    private int health;
    private int foodLevel;
    private int experienceLevel;
    private long capturedAtMillis;

    public void capture(String playerName, int health, int foodLevel,
                        int experienceLevel, long capturedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.health = health;
        this.foodLevel = foodLevel;
        this.experienceLevel = experienceLevel;
        this.capturedAtMillis = capturedAtMillis;
    }

    public String getPlayerName() { return playerName; }
    public int getHealth() { return health; }
    public int getFoodLevel() { return foodLevel; }
    public int getExperienceLevel() { return experienceLevel; }
    public long getCapturedAtMillis() { return capturedAtMillis; }
}