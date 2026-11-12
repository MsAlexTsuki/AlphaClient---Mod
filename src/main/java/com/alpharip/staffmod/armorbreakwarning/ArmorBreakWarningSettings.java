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
        int next = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (THRESHOLDS[i] == thresholdPercent) {
                next = (i + 1) % THRESHOLDS.length;
                thresholdPercent = THRESHOLDS[next];
                return;
            }
            if (THRESHOLDS[i] > thresholdPercent) {
                thresholdPercent = THRESHOLDS[i];
                return;
            }
        }
        thresholdPercent = THRESHOLDS[next];
    }

    int getBreakThreshold(ItemDurability durability) {
        if (durability == null || durability.maximum <= 0) return 0;
        return Math.max(1, (durability.maximum * thresholdPercent) / 100);
    }

    /** Small immutable input used to keep the threshold calculation independent of Minecraft classes. */
    static final class ItemDurability {
        final int maximum;

        ItemDurability(int maximum) {
            this.maximum = maximum;
        }
    }
}
