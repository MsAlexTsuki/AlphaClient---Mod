package com.alpharip.staffmod.AfkCamera;

/** Session-local configuration for the AFK Camera prototype. */
public final class AfkCameraSettings {
    public static final int MIN_IDLE_DELAY_SECONDS = 5;
    public static final int MAX_IDLE_DELAY_SECONDS = 600;
    public static final int DEFAULT_IDLE_DELAY_SECONDS = 60;

    private boolean enabled;
    private int idleDelaySeconds = DEFAULT_IDLE_DELAY_SECONDS;
    private AfkCameraStyle style = AfkCameraStyle.DEFAULT;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean value) { enabled = value; }

    public synchronized int getIdleDelaySeconds() { return idleDelaySeconds; }
    public synchronized void setIdleDelaySeconds(int value) {
        idleDelaySeconds = Math.max(MIN_IDLE_DELAY_SECONDS,
                Math.min(MAX_IDLE_DELAY_SECONDS, value));
    }

    public synchronized AfkCameraStyle getStyle() { return style; }
    public synchronized void setStyle(AfkCameraStyle value) {
        style = value == null ? AfkCameraStyle.DEFAULT : value;
    }

    public synchronized void reset() {
        enabled = false;
        idleDelaySeconds = DEFAULT_IDLE_DELAY_SECONDS;
        style = AfkCameraStyle.DEFAULT;
    }
}
