package com.alpharip.staffmod.fishingbitealarm;

/** Validated local settings. The interval is interpreted as the minimum gap between alerts. */
public final class FishingBiteAlarmSettings {
    private boolean enabled = true;
    private int intervalSeconds = 60;
    private float volume = 1.0F;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }

    public synchronized int getIntervalSeconds() { return intervalSeconds; }
    public synchronized void setIntervalSeconds(int seconds) {
        intervalSeconds = Math.max(1, Math.min(600, seconds));
    }

    public synchronized float getVolume() { return volume; }
    public synchronized void setVolume(float volume) {
        if (Float.isNaN(volume) || Float.isInfinite(volume)) return;
        this.volume = Math.max(0.0F, Math.min(1.0F, volume));
    }

    public synchronized void adjustIntervalSeconds(int amount) {
        setIntervalSeconds(intervalSeconds + amount);
    }

    public synchronized void adjustVolume(float amount) {
        setVolume(volume + amount);
    }
}
