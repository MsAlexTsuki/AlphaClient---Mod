package com.alpharip.staffmod.actingrankassignment;

/** Session-local settings for the acting-rank prototype. */
public final class ActingRankSettings {
    private boolean enabled = true;
    private boolean showExpired = true;
    private int maximumEntries = 250;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isShowExpired() { return showExpired; }
    public void setShowExpired(boolean showExpired) { this.showExpired = showExpired; }
    public int getMaximumEntries() { return maximumEntries; }

    public void setMaximumEntries(int maximumEntries) {
        if (maximumEntries < 1) throw new IllegalArgumentException("Maximum entries must be positive.");
        this.maximumEntries = maximumEntries;
    }
}
