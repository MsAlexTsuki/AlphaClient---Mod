package com.alpharip.staffmod.AdjustableRainAndSnowSpeed;

/** Immutable read model for the current prototype settings. */
public final class WeatherSpeedSnapshot {
    private final boolean enabled;
    private final boolean rainEnabled;
    private final boolean snowEnabled;
    private final float rainMultiplier;
    private final float snowMultiplier;

    public WeatherSpeedSnapshot(WeatherSpeedSettings settings) {
        if (settings == null) throw new IllegalArgumentException("settings cannot be null");
        enabled = settings.isEnabled();
        rainEnabled = settings.isRainEnabled();
        snowEnabled = settings.isSnowEnabled();
        rainMultiplier = settings.getRainMultiplier();
        snowMultiplier = settings.getSnowMultiplier();
    }

    public boolean isEnabled() { return enabled; }
    public boolean isRainEnabled() { return rainEnabled; }
    public boolean isSnowEnabled() { return snowEnabled; }
    public float getRainMultiplier() { return rainMultiplier; }
    public float getSnowMultiplier() { return snowMultiplier; }
}
