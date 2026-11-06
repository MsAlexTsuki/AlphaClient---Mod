package com.alpharip.staffmod.AdjustableRainAndSnowSpeed;

/** Weather channels supported by the prototype. */
public enum WeatherType {
    RAIN("Rain"),
    SNOW("Snow");

    private final String displayName;

    WeatherType(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }
}
