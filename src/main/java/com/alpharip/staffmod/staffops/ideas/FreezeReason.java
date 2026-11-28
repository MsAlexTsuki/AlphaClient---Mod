package com.alpharip.staffmod.staffops.ideas;

/**
 * Local data model for a displayed freeze reason.
 * Setting a reason here does not freeze a player or notify a server.
 */
public final class FreezeReason {
    private String playerName = "";
    private String reason = "";
    private long updatedAtMillis;

    public void set(String playerName, String reason, long updatedAtMillis) {
        this.playerName = playerName == null ? "" : playerName.trim();
        this.reason = reason == null ? "" : reason.trim();
        this.updatedAtMillis = updatedAtMillis;
    }

    public void clear() {
        playerName = "";
        reason = "";
        updatedAtMillis = 0L;
    }

    public String getPlayerName() { return playerName; }
    public String getReason() { return reason; }
    public long getUpdatedAtMillis() { return updatedAtMillis; }
}