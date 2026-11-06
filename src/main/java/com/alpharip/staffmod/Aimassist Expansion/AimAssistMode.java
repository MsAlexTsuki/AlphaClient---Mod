package com.alpharip.staffmod.aimassistexpansion;

/** Available correction profiles for the Aim Assist expansion prototype. */
public enum AimAssistMode {
    SMOOTH("Smooth Aim"),
    SNAP("Snap Aim"),
    DYNAMIC("Dynamic Aim"),
    PREDICTIVE("Predictive Aim"),
    HORIZONTAL("Horizontal Aim"),
    VERTICAL("Vertical Aim"),
    ADAPTIVE("Adaptive Aim");

    private final String displayName;
    AimAssistMode(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
