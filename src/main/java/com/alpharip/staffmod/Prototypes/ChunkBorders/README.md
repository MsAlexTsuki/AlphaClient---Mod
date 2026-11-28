# Chunk Borders

## Purpose

Chunk Borders is a standalone Minecraft 1.8.9 / Forge prototype for visualizing chunk boundaries around the local player. It draws vertical corner pillars through the world and horizontal boundary grid lines at 16-block Y intervals. The current chunk can also be outlined at the player's current block height.

The prototype only renders a visual overlay. It does not modify chunks, blocks, world data, or server state.

## Original idea settings

The original `staffops/ideas/ChunkBorders.java` contained:

- `enabled = false`
- `category = "World/diagnostics"`
- `style = "default"`

The disabled default and default style are preserved. The category is retained as descriptive metadata on the module facade; production category registration is not part of this prototype.

## Included parts

| File | Responsibility |
|---|---|
| ChunkBorders.java | Standalone module facade, explicit Forge event lifecycle, settings access, GUI entry point, and local persistence |
| ChunkBordersSettings.java | Validated enablement, style, render radius, line toggles, current-chunk highlight, and opacity |
| ChunkBorderStyle.java | Default, Minimal, and High contrast visual presets |
| ChunkBordersSnapshot.java | Immutable render-time settings and player chunk coordinates |
| ChunkBordersRenderer.java | Camera-relative OpenGL rendering of vertical pillars, horizontal grid lines, and current-chunk outline |
| ChunkBordersEventHandler.java | Forge world-render event adapter; renders only while the module is running and enabled |
| ChunkBordersGui.java | Settings screen for enablement, style, radius, line visibility, highlight, and opacity |
| ChunkBordersStorage.java | Local properties-file settings persistence |

## Controls

- **Chunk borders:** Disabled by default, preserving the original idea setting.
- **Style:** Default, Minimal, or High contrast.
- **Render radius:** 1–8 chunks outward from the player's current chunk; default 2.
- **Vertical corner pillars:** Draws vertical lines at chunk-grid intersections from Y=0 to Y=256.
- **Horizontal grid:** Draws chunk boundary segments at every 16-block Y interval from Y=0 to Y=256.
- **Highlight current chunk:** Draws an outline of the player's current chunk at the player's block Y.
- **Opacity:** Adjustable overlay opacity.

Settings are stored locally at:

`config/alphaclient/chunk_borders.properties`

## Lifecycle and integration boundary

The module registers its Forge render listener only when `start()` is called and unregisters it when `stop()` is called. It is not auto-registered, auto-started, or wired into AlphaClient Core. `openSettings(parent)` is the standalone GUI entry point; an approved production menu route has not been added.

No Supabase calls, remote persistence, Alpha Launcher changes, or AlphaClient Core changes are included.

## Current status

**Present in prototype:** standalone module facade; explicit event lifecycle; chunk coordinate calculation; vertical pillar and horizontal grid rendering; current-chunk highlight; three visual styles; configurable radius, visibility toggles, and opacity; settings GUI; local settings persistence.

**Missing / not integrated:** AlphaClient Core/module-menu registration and an approved route to open the GUI.

**Not verified:** compilation against the exact ForgeGradle setup; visual accuracy in Minecraft 1.8.9 / Forge 11.15.1.2318; OpenGL state interaction with other overlays; performance at larger render radii; GUI layout at different resolutions; and settings save/load round trips. These must be checked before claiming the feature works in-game.
