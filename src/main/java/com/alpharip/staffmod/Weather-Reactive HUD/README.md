# Weather-Reactive HUD Prototype

A small presentation model that changes HUD colors according to the current weather.

## Included
- Clear, rain, thunder, and snow conditions.
- Separate primary, secondary, and accent ARGB colors.
- A resolver that maps a weather condition to a palette.
- A model that keeps the active condition and exposes the matching palette.

## Not included / not verified
- No Minecraft world-weather adapter or HUD renderer is connected yet.
- Snow detection must be supplied by a future biome/weather adapter.
- No transition animation, user palette settings, or custom HUD screen.
- No AlphaClient Core integration.
- Forge compilation and in-game rendering have not been tested.
