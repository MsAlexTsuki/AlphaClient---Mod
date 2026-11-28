package com.alpharip.staffmod.trackers.acknowledgement;

/** Session-local display and capacity settings for the prototype. */
public final class AcknowledgementSettings {
    private boolean enabled = true;
    private boolean showAcknowledged = true;
    private final int maximumEntries;

    public AcknowledgementSettings() {
        this(200);
    }

    public AcknowledgementSettings(int maximumEntries) {
        if (maximumEntries < 1 || maximumEntries > 5000) {
            throw new IllegalArgumentException("maximumEntries must be between 1 and 5000.");
        }
        this.maximumEntries = maximumEntries;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isShowAcknowledged() { return showAcknowledged; }
    public void setShowAcknowledged(boolean showAcknowledged) {
        this.showAcknowledged = showAcknowledged;
    }
    public int getMaximumEntries() { return maximumEntries; }
}
