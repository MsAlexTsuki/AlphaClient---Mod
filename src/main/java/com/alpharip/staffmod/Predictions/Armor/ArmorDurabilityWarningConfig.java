package com.alpharip.staffmod.predictions.armor;

/** Immutable local settings for the equipped-armor durability warning. */
public final class ArmorDurabilityWarningConfig {
    private final int warningThresholdPercent;
    private final int hudX;
    private final int hudY;
    private final int hudColor;

    public ArmorDurabilityWarningConfig(int warningThresholdPercent, int hudX, int hudY, int hudColor) {
        if (warningThresholdPercent < 0 || warningThresholdPercent > 100) {
            throw new IllegalArgumentException("warningThresholdPercent must be between 0 and 100.");
        }
        this.warningThresholdPercent = warningThresholdPercent;
        this.hudX = hudX;
        this.hudY = hudY;
        this.hudColor = hudColor;
    }

    public static ArmorDurabilityWarningConfig defaults() {
        return new ArmorDurabilityWarningConfig(10, 6, 18, 0xFFFF5555);
    }

    public int getWarningThresholdPercent() { return warningThresholdPercent; }
    public int getHudX() { return hudX; }
    public int getHudY() { return hudY; }
    public int getHudColor() { return hudColor; }
}
