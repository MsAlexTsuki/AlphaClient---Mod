package com.alpharip.staffmod.aimpatternlibrary;

/** Prototype categories; these are organizational labels, not aim modes. */
public enum AimPatternCategory {
    GENERAL("General"),
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical"),
    SMOOTHING("Smoothing"),
    TARGETING("Targeting"),
    OTHER("Other");

    private final String displayName;
    AimPatternCategory(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
    public AimPatternCategory next() {
        AimPatternCategory[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
