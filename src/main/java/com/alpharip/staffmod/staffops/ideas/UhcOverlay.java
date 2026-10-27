package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for a UHC-focused match overlay.
 * Match detection and HUD rendering are not connected yet.
 */
public final class UhcOverlay {
    public static final String FEATURE_NAME = "UHC Overlay";

    private boolean enabled = false;
    private boolean showGoldenHeads = true;
    private boolean showHealingItems = true;
    private boolean showArrows = true;
    private boolean showKills = true;
    private boolean showMatchTimer = true;
    private int goldenHeads;
    private int healingItems;
    private int arrows;
    private int kills;
    private long matchStartMillis;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isShowGoldenHeads() { return showGoldenHeads; }
    public void setShowGoldenHeads(boolean value) { showGoldenHeads = value; }
    public boolean isShowHealingItems() { return showHealingItems; }
    public void setShowHealingItems(boolean value) { showHealingItems = value; }
    public boolean isShowArrows() { return showArrows; }
    public void setShowArrows(boolean value) { showArrows = value; }
    public boolean isShowKills() { return showKills; }
    public void setShowKills(boolean value) { showKills = value; }
    public boolean isShowMatchTimer() { return showMatchTimer; }
    public void setShowMatchTimer(boolean value) { showMatchTimer = value; }

    public int getGoldenHeads() { return goldenHeads; }
    public void setGoldenHeads(int value) { goldenHeads = Math.max(0, value); }
    public int getHealingItems() { return healingItems; }
    public void setHealingItems(int value) { healingItems = Math.max(0, value); }
    public int getArrows() { return arrows; }
    public void setArrows(int value) { arrows = Math.max(0, value); }
    public int getKills() { return kills; }
    public void setKills(int value) { kills = Math.max(0, value); }
    public long getMatchStartMillis() { return matchStartMillis; }
    public void startMatch(long nowMillis) { matchStartMillis = Math.max(0L, nowMillis); }
    public long getElapsedMillis(long nowMillis) {
        if (matchStartMillis <= 0L || nowMillis <= matchStartMillis) return 0L;
        return nowMillis - matchStartMillis;
    }

    public void resetMatch() {
        goldenHeads = 0;
        healingItems = 0;
        arrows = 0;
        kills = 0;
        matchStartMillis = 0L;
    }
}
