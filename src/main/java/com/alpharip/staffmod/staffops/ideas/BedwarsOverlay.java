package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for a BedWars match overlay.
 * Match detection, server-specific parsing, and HUD rendering are not connected yet.
 */
public final class BedwarsOverlay {
    public static final String FEATURE_NAME = "BedWars Overlay";

    private boolean enabled;
    private int ownBeds = -1;
    private int enemyBeds = -1;
    private int kills;
    private int finalKills;
    private String teamStatus = "Unknown";
    private long matchStartedAt;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public int getOwnBeds() { return ownBeds; }
    public int getEnemyBeds() { return enemyBeds; }
    public int getKills() { return kills; }
    public int getFinalKills() { return finalKills; }
    public String getTeamStatus() { return teamStatus; }

    public void startMatch(long nowMillis) {
        matchStartedAt = Math.max(1L, nowMillis);
        ownBeds = -1;
        enemyBeds = -1;
        kills = 0;
        finalKills = 0;
        teamStatus = "Unknown";
    }

    public long getElapsedSeconds(long nowMillis) {
        return matchStartedAt == 0L ? 0L : Math.max(0L, (nowMillis - matchStartedAt) / 1000L);
    }

    public void setBedCounts(int own, int enemies) {
        ownBeds = validCount(own);
        enemyBeds = validCount(enemies);
    }

    public void recordKill(boolean finalKill) {
        kills++;
        if (finalKill) finalKills++;
    }

    public void setTeamStatus(String status) {
        teamStatus = status == null || status.trim().isEmpty() ? "Unknown" : status.trim();
    }

    public void reset() {
        matchStartedAt = 0L;
        ownBeds = -1;
        enemyBeds = -1;
        kills = 0;
        finalKills = 0;
        teamStatus = "Unknown";
    }

    private int validCount(int value) {
        return value < 0 ? -1 : Math.min(value, 1000);
    }
}
