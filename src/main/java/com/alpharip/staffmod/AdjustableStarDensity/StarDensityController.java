package com.alpharip.staffmod.AdjustableStarDensity;

/** Coordinates validated local changes and exposes read-only snapshots. */
public final class StarDensityController {
    private final StarDensitySettings settings;

    public StarDensityController(StarDensitySettings settings) {
        if (settings == null) throw new IllegalArgumentException("settings cannot be null");
        this.settings = settings;
    }

    public StarDensitySnapshot snapshot() { return new StarDensitySnapshot(settings); }
    public void setEnabled(boolean enabled) { settings.setEnabled(enabled); }
    public void setDensityPercent(int percent) { settings.setDensityPercent(percent); }
    public void cycleStyle() { settings.setStyle(settings.getStyle().next()); }
    public void reset() { settings.reset(); }
}
