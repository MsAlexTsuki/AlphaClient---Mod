package com.alpharip.staffmod.AdjustableRainAndSnowSpeed;

/** Applies validated local setting changes and exposes effective speed values. */
public final class WeatherSpeedController {
    private final WeatherSpeedSettings settings;

    public WeatherSpeedController(WeatherSpeedSettings settings) {
        if (settings == null) throw new IllegalArgumentException("settings cannot be null");
        this.settings = settings;
    }

    public WeatherSpeedSnapshot snapshot() { return new WeatherSpeedSnapshot(settings); }

    public float getEffectiveMultiplier(WeatherType type) {
        return settings.getEffectiveMultiplier(type);
    }

    public void setChannelEnabled(WeatherType type, boolean enabled) {
        if (type == WeatherType.RAIN) settings.setRainEnabled(enabled);
        else if (type == WeatherType.SNOW) settings.setSnowEnabled(enabled);
    }

    public void setMultiplier(WeatherType type, float multiplier) {
        if (type == WeatherType.RAIN) settings.setRainMultiplier(multiplier);
        else if (type == WeatherType.SNOW) settings.setSnowMultiplier(multiplier);
    }
}
