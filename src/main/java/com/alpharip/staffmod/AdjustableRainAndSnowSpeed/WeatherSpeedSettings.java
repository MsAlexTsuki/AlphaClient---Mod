package com.alpharip.staffmod.AdjustableRainAndSnowSpeed;

/**
 * In-memory settings for separate rain and snow speed multipliers.
 * Values are presentation/calculation settings only until a renderer adapter is approved.
 */
public final class WeatherSpeedSettings {
    public static final float MIN_MULTIPLIER = 0.25F;
    public static final float MAX_MULTIPLIER = 3.0F;
    public static final float DEFAULT_MULTIPLIER = 1.0F;

    private boolean enabled = true;
    private boolean rainEnabled = true;
    private boolean snowEnabled = true;
    private float rainMultiplier = DEFAULT_MULTIPLIER;
    private float snowMultiplier = DEFAULT_MULTIPLIER;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean value) { enabled = value; }
    public synchronized boolean isRainEnabled() { return rainEnabled; }
    public synchronized void setRainEnabled(boolean value) { rainEnabled = value; }
    public synchronized boolean isSnowEnabled() { return snowEnabled; }
    public synchronized void setSnowEnabled(boolean value) { snowEnabled = value; }
    public synchronized float getRainMultiplier() { return rainMultiplier; }
    public synchronized float getSnowMultiplier() { return snowMultiplier; }

    public synchronized void setRainMultiplier(float value) {
        rainMultiplier = normalize(value);
    }

    public synchronized void setSnowMultiplier(float value) {
        snowMultiplier = normalize(value);
    }

    public synchronized void reset() {
        enabled = true;
        rainEnabled = true;
        snowEnabled = true;
        rainMultiplier = DEFAULT_MULTIPLIER;
        snowMultiplier = DEFAULT_MULTIPLIER;
    }

    public synchronized float getEffectiveMultiplier(WeatherType type) {
        if (!enabled || type == null) return 1.0F;
        if (type == WeatherType.RAIN) return rainEnabled ? rainMultiplier : 1.0F;
        return snowEnabled ? snowMultiplier : 1.0F;
    }

    private static float normalize(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return DEFAULT_MULTIPLIER;
        float clamped = Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, value));
        return Math.round(clamped * 100.0F) / 100.0F;
    }
}
