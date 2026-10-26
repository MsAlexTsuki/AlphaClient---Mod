package com.alpharip.staffmod.staffops.ideas;

/** Presentation data for a timestamp overlay; rendering is handled elsewhere. */
public final class EvidenceTimestampOverlay {
    private long timestampMillis;
    private String label = "";
    private boolean visible;

    public void show(long timestampMillis, String label) {
        this.timestampMillis = timestampMillis;
        this.label = label == null ? "" : label;
        this.visible = true;
    }

    public void hide() {
        visible = false;
        label = "";
    }

    public long getTimestampMillis() { return timestampMillis; }
    public String getLabel() { return label; }
    public boolean isVisible() { return visible; }
}