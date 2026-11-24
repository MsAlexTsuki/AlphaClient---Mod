package com.alpharip.staffmod;

/** ARGB colors selected for a weather condition. */
public final class HudWeatherPalette {
    private final int primary;
    private final int secondary;
    private final int accent;

    public HudWeatherPalette(int primary, int secondary, int accent) {
        this.primary = primary;
        this.secondary = secondary;
        this.accent = accent;
    }

    public int getPrimary() { return primary; }
    public int getSecondary() { return secondary; }
    public int getAccent() { return accent; }
}
