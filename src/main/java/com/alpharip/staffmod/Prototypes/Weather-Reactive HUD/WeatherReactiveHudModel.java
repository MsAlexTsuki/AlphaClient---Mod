package com.alpharip.staffmod;

/** Holds the current weather state and its resolved HUD palette. */
public final class WeatherReactiveHudModel {
    private final WeatherPaletteResolver resolver;
    private WeatherCondition condition = WeatherCondition.CLEAR;

    public WeatherReactiveHudModel(WeatherPaletteResolver resolver) {
        if (resolver == null) throw new IllegalArgumentException("resolver must not be null");
        this.resolver = resolver;
    }

    public void update(WeatherCondition nextCondition) {
        condition = nextCondition == null ? WeatherCondition.CLEAR : nextCondition;
    }

    public WeatherCondition getCondition() { return condition; }
    public HudWeatherPalette getPalette() { return resolver.resolve(condition); }
}
