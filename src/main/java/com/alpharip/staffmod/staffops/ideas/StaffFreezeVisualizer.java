package com.alpharip.staffmod.staffops.ideas;

/**
 * Client-side presentation state for displaying a reported freeze state.
 * This is visual-only and cannot freeze, unfreeze, or restrict another player.
 */
public final class StaffFreezeVisualizer {
    private String playerName = "";
    private String reason = "";
    private boolean displayed;
    private long observedAtMillis;

    public void show(String playerName, String reason, long observedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.reason = reason == null ? "" : reason;
        this.observedAtMillis = observedAtMillis;
        this.displayed = true;
    }

    public void hide() {
        displayed = false;
        playerName = "";
        reason = "";
        observedAtMillis = 0L;
    }

    public boolean isDisplayed() { return displayed; }
    public String getPlayerName() { return playerName; }
    public String getReason() { return reason; }
    public long getObservedAtMillis() { return observedAtMillis; }

    // TODO(Supabase): retrieve authoritative freeze display data from an authorized source.
}