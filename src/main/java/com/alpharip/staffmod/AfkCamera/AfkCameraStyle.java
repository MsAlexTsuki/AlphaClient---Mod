package com.alpharip.staffmod.AfkCamera;

/**
 * Proposed camera behavior presets.
 * These are labels/state only; no camera movement is implemented.
 */
public enum AfkCameraStyle {
    DEFAULT("Default"),
    SLOW_ORBIT("Slow Orbit"),
    GENTLE_SWEEP("Gentle Sweep");

    private final String displayName;

    AfkCameraStyle(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    public AfkCameraStyle next() {
        AfkCameraStyle[] values = values();
        return values()[(ordinal() + 1) % values.length];
    }
}
