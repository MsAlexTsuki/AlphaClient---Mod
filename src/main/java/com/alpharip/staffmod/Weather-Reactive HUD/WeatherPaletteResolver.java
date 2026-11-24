package com.alpharip.staffmod;

/** Maps weather conditions to a consistent set of HUD colors. */
public final class WeatherPaletteResolver {
    private static final HudWeatherPalette CLEAR =
            new HudWeatherPalette(0xFFFFC83D, 0xFFE7E7E7, 0xFF87CEFA);
    private static final HudWeatherPalette RAIN =
            new HudWeatherPalette(0xFF6F9FCB, 0xFFB7C8D8, 0xFF477AA8);
    private static final HudWeatherPalette THUNDER =
            new HudWeatherPalette(0xFF77718F, 0xFFB8B4C5, 0xFF514B69);
    private static final HudWeatherPalette SNOW =
            new HudWeatherPalette(0xFFE8F4FF, 0xFFBFD8EE, 0xFF8EB9DC);

    public HudWeatherPalette resolve(WeatherCondition condition) {
        if (condition == null) condition = WeatherCondition.CLEAR;
        switch (condition) {
            case RAIN: return RAIN;
            case THUNDER: return THUNDER;
            case SNOW: return SNOW;
            case CLEAR:
            default: return CLEAR;
        }
    }
}
