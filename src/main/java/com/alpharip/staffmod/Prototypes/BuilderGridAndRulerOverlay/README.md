# Builder Grid & Ruler Overlay

## Purpose

Builder Grid & Ruler Overlay is a standalone Minecraft 1.8.9 / Forge prototype for construction alignment. It draws a horizontal world-space grid around a saved origin and a ruler from that origin to the player's current block position.

The overlay is visual only. It does not place, break, or modify blocks and does not change world data.

## Original idea settings

The original `staffops/ideas/BuilderGridAndRulerOverlay.java` contained:

- `gridEnabled = true`
- `rulerEnabled = true`
- `gridSpacing = 1`
- Grid spacing clamped to 1–64
- Coordinate snapping to the nearest multiple of the configured spacing

All original defaults and the snapping rule are retained.

## Included parts

| File | Responsibility |
|---|---|
| BuilderGridAndRulerOverlay.java | Standalone module facade, explicit Forge event lifecycle, anchor management, snap API, GUI entry point, and local persistence |
| BuilderGridSettings.java | Independent grid/ruler toggles, spacing, and guide-area radius |
| BuilderGridAnchor.java | Saved world-space origin for grid and measurements |
| BuilderGridSnapshot.java | Immutable render-time copy of settings, origin, and player block coordinates |
| BuilderGridCalculator.java | Pure nearest-multiple snapping and bounded grid-coordinate calculations |
| BuilderGridRenderer.java | Camera-relative horizontal grid, X/Z ruler axes, and block tick marks |
| BuilderGridRulerHudRenderer.java | HUD readout of X/Y/Z offsets from the saved origin and active spacing |
| BuilderGridEventHandler.java | Forge world-render and HUD event adapter |
| BuilderGridGui.java | Settings screen for grid, ruler, spacing, area radius, and origin capture |
| BuilderGridStorage.java | Local settings and origin persistence |

## Behavior and controls

- **Build grid:** Enabled by default. Draws a horizontal grid on the saved origin's Y level.
- **Ruler:** Enabled by default. Draws X and Z measurement axes from the saved origin toward the player's current block position and displays X/Y/Z offsets in the HUD.
- **Grid spacing:** Defaults to 1 block and cycles through 1, 2, 4, 8, 16, 32, and 64 blocks.
- **Guide area radius:** Defaults to 16 blocks around the saved origin; configurable from 8 to 64 blocks.
- **Origin:** Captured from the player's current block position using the settings screen. If no origin has been saved, the first active world render captures the current player block position.
- **Snapping:** `snap(double)` and `snapCoordinate(int)` round coordinates to the nearest multiple of the configured spacing, preserving the original idea's behavior.

Settings and origin are stored locally at:

`config/alphaclient/builder_grid_ruler.properties`

## Lifecycle and integration boundary

The Forge event listener is registered only when `start()` is called and removed by `stop()`. The prototype does not auto-register, auto-start, or integrate itself into AlphaClient Core. `openSettings(parent)` is the standalone GUI entry point; an approved production menu route has not been added.

No Supabase calls, remote persistence, Alpha Launcher changes, or AlphaClient Core changes are included.

## Current status

**Present in prototype:** settings model; original snapping behavior; origin capture and persistence; grid coordinate calculations; world-space grid and ruler geometry; X/Y/Z HUD readout; settings GUI; local properties storage.

**Missing / not integrated:** AlphaClient Core/module-menu registration and an approved route to open the GUI.

**Not verified:** compilation against the exact ForgeGradle setup; visual behavior in Minecraft 1.8.9 / Forge 11.15.1.2318; OpenGL state interaction with other overlays; ruler appearance while moving and at different elevations; GUI layout at different resolutions; and settings/origin save-load round trips. These must be checked before claiming the feature works in-game.
