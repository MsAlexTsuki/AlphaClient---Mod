package com.alpharip.staffmod.AdjustableStarDensity;

/** Visual style options reserved for a future star renderer adapter. */
public enum StarDensityStyle {
    DEFAULT("Default"),
    SOFT("Soft"),
    BRIGHT("Bright");

    private final String displayName;

    StarDensityStyle(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    public StarDensityStyle next() {
        StarDensityStyle[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
