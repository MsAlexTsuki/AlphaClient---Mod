# Build Symmetry Guides

## Purpose

Build Symmetry Guides is a standalone Minecraft 1.8.9 / Forge prototype for visual block-placement symmetry assistance. It samples the block currently targeted by the player and calculates corresponding block coordinates across one or two vertical mirror planes.

The prototype only draws visual guides. It does not place, remove, copy, or modify blocks.

## Mirror rules

- **X plane:** reflect the target block's X coordinate around the center of the configured origin block.
- **Z plane:** reflect the target block's Z coordinate around the center of the configured origin block.
- **X + Z planes:** calculate the X reflection, Z reflection, and combined X/Z reflection.
- The Y coordinate is preserved.
- A reflected coordinate equal to the source coordinate is omitted, and duplicate destinations are removed.

For a source X coordinate x and origin X coordinate originX, the reflected X coordinate is 2 * originX - x. The Z calculation follows the same rule. The mirror planes pass through the center of the origin block.

## Included parts

| File | Responsibility |
|---|---|
| BuildSymmetryGuides.java | Standalone module facade, explicit Forge event lifecycle, target snapshot, settings entry point |
| BuildSymmetryGuideConfig.java | Validated local settings and defaults |
| SymmetryAxis.java | X, Z, and combined mirror-plane selection |
| BlockCoordinates.java | Immutable block-coordinate value object |
| SymmetryGuidePoint.java | One reflected destination and its reflection label |
| SymmetryCalculator.java | Pure coordinate-reflection calculations; no world mutation |
| SymmetryGuideSnapshot.java | Immutable render-ready state |
| SymmetryGuideColor.java | Local overlay color palette |
| BuildSymmetryGuidesEventHandler.java | Client-tick target sampling and world-render event adapter |
| BuildSymmetryGuidesRenderer.java | Axis guide lines, source-to-mirror connection lines, and mirrored block outlines |
| BuildSymmetryGuidesGui.java | Settings screen with preview toggles and precise origin controls |
| BuildSymmetryGuideStorage.java | Local properties-file settings persistence |

## Controls and settings

The GUI exposes module enablement, mirror plane, mirror preview, axis guide lines, source-to-mirror connection lines, guide distance, overlay opacity, and color. The origin can be set to the player's current X/Z block position or adjusted one block at a time along either axis.

Settings are stored locally at:

`config/alphaclient/build_symmetry_guides.properties`

The module is not started automatically. A future integration may call `start()` and `stop()` through an approved lifecycle and provide an approved way to open `openSettings(parent)`.

## Current status

**Present in prototype:** coordinate reflection for X, Z, and combined planes; duplicate/source-position filtering; immutable render snapshot; target sampling; world-space guide rendering; source-to-mirror connection lines; settings GUI with one-block origin adjustment; bounded settings; local settings load/save; explicit event registration lifecycle.

**Missing / not integrated:** AlphaClient Core module registration, a live module-menu entry, an approved keybind or menu route to open the GUI, and any production integration with the existing building or placement systems. These are intentionally outside the current prototype scope.

**Not verified:** compilation against the repository's exact ForgeGradle setup, rendering behavior in Minecraft 1.8.9, settings round-trip behavior in-game, and visual correctness at different GUI scales and render distances. The prototype has not been built or run as part of this change.

No Supabase calls, remote persistence, Alpha Launcher changes, or AlphaClient Core changes are included.
