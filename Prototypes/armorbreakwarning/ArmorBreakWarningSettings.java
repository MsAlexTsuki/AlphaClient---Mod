package com.alpharip.staffmod.armorbreakwarning;

/** Local settings for the Armor Break Warning prototype. */
public final class ArmorBreakWarningSettings {
    private static final int[] THRESHOLDS = {5, 10, 15, 20, 25};

    private boolean enabled = true;
    private int thresholdPercent = 10;

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public synchronized int getThresholdPercent() {
        return thresholdPercent;
    }

    public synchronized void setThresholdPercent(int value) {
        thresholdPercent = Math.max(1, Math.min(100, value));
    }

    public synchronized void cycleThreshold() {
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (THRESHOLDS[i] == thresholdPercent) {
                thresholdPercent = THRESHOLDS[(i + 1) % THRESHOLDS.length];
                return;
            }
            if (THRESHOLDS[i] > thresholdPercent) {
                thresholdPercent = THRESHOLDS[i];
                return;
            }
        }
        thresholdPercent = THRESHOLDS[0];
    }
}
