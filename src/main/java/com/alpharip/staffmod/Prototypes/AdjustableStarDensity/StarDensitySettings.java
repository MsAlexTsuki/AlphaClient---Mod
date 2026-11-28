package com.alpharip.staffmod.AdjustableStarDensity;

/** In-memory settings for the star-density prototype. */
public final class StarDensitySettings {
    public static final int MIN_DENSITY = 0;
    public static final int MAX_DENSITY = 200;
    public static final int DEFAULT_DENSITY = 100;

    private boolean enabled = true;
    private int densityPercent = DEFAULT_DENSITY;
    private StarDensityStyle style = StarDensityStyle.DEFAULT;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean value) { enabled = value; }
    public synchronized int getDensityPercent() { return densityPercent; }

    public synchronized void setDensityPercent(int value) {
        densityPercent = Math.max(MIN_DENSITY, Math.min(MAX_DENSITY, value));
    }

    public synchronized StarDensityStyle getStyle() { return style; }
    public synchronized void setStyle(StarDensityStyle value) {
        style = value == null ? StarDensityStyle.DEFAULT : value;
    }

    public synchronized void reset() {
        enabled = true;
        densityPercent = DEFAULT_DENSITY;
        style = StarDensityStyle.DEFAULT;
    }

    /** Returns the multiplier for a future renderer; disabled means vanilla density. */
    public synchronized float getEffectiveDensityMultiplier() {
        return enabled ? densityPercent / 100.0F : 1.0F;
    }
}
