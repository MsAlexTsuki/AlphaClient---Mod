package com.alpharip.staffmod.AdjustableStarDensity;

/** Immutable view of the current local star-density settings. */
public final class StarDensitySnapshot {
    private final boolean enabled;
    private final int densityPercent;
    private final StarDensityStyle style;
    private final float effectiveMultiplier;

    public StarDensitySnapshot(StarDensitySettings settings) {
        if (settings == null) throw new IllegalArgumentException("settings cannot be null");
        this.enabled = settings.isEnabled();
        this.densityPercent = settings.getDensityPercent();
        this.style = settings.getStyle();
        this.effectiveMultiplier = settings.getEffectiveDensityMultiplier();
    }

    public boolean isEnabled() { return enabled; }
    public int getDensityPercent() { return densityPercent; }
    public StarDensityStyle getStyle() { return style; }
    public float getEffectiveMultiplier() { return effectiveMultiplier; }
}
