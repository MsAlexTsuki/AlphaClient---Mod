package com.alpharip.staffmod.staffops.ideas;

/**
 * Local model for latency values supplied by an existing client connection.
 * Other players' ping values require an available server/player-list source.
 */
public final class PlayerPingViewer {
    private String playerName = "";
    private int pingMillis = -1;
    private long capturedAtMillis;

    public void update(String playerName, int pingMillis, long capturedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.pingMillis = pingMillis < 0 ? -1 : pingMillis;
        this.capturedAtMillis = capturedAtMillis;
    }

    public boolean hasPingValue() { return pingMillis >= 0; }
    public String getPlayerName() { return playerName; }
    public int getPingMillis() { return pingMillis; }
    public long getCapturedAtMillis() { return capturedAtMillis; }

    // TODO(Supabase): do not use Supabase for live ping; use the approved game/server source if available.
}