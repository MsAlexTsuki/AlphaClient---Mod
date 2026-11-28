package com.alpharip.staffmod.staffops.ideas;

/**
 * Tracks simple client-side session counters for a future HUD.
 * Event hooks and HUD rendering are not connected yet.
 */
public final class SessionStatsHud {
    public static final String FEATURE_NAME = "Session Stats HUD";

    private boolean enabled;
    private long sessionStartedAt;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }

    public void startSession(long nowMillis) {
        sessionStartedAt = Math.max(1L, nowMillis);
        kills = 0;
        deaths = 0;
        wins = 0;
        losses = 0;
    }

    public long getPlayTimeSeconds(long nowMillis) {
        return sessionStartedAt == 0L ? 0L : Math.max(0L, (nowMillis - sessionStartedAt) / 1000L);
    }

    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }

    public double getKillDeathRatio() {
        if (deaths == 0) return kills == 0 ? 0.0D : kills;
        return (double) kills / (double) deaths;
    }

    public void recordKill() { kills++; }
    public void recordDeath() { deaths++; }
    public void recordWin() { wins++; }
    public void recordLoss() { losses++; }

    public void reset() {
        sessionStartedAt = 0L;
        kills = 0;
        deaths = 0;
        wins = 0;
        losses = 0;
    }
}
