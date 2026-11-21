# Hitbox Prototype

Package: `com.alpharip.staffmod.hitbox`

This is an isolated Minecraft 1.8.9 / Forge 11.15.1.2318 prototype. It is placed beside `staffops`, not inside it.

## Included

- `HitboxModule`: lifecycle wrapper with explicit Forge event-bus registration.
- `HitboxSettings`: enable state, render range, line width, RGB/alpha color, invisible-player filter, and through-walls option.
- `HitboxRenderer`: draws the bounding boxes of other living player entities.
- `HitboxGeometry`: interpolates entity positions and converts bounding boxes to render-view-relative coordinates.

## Current behavior

- Only other alive player entities are drawn.
- Default range is 32 blocks.
- Default color is Alpha gold.
- Invisible players are hidden by default.
- Through-walls rendering is disabled by default.
- The renderer only visualizes the existing bounding box; it does not modify hit detection or collision geometry.
- The module is disabled by default.

## Integration boundary

The prototype is deliberately not registered in `AlphaClient.initialize()`, is not added to the module registry, and has no menu/config persistence integration. Those changes are outside this prototype folder and require a separate integration decision.

To activate it from an approved integration point, create and retain a `HitboxModule` instance, call `register()`, and then call `setEnabled(true)`. Call `unregister()` when the integration is shut down.

## Verification

Source files were created for the `mod-ideas` branch. No Gradle build or in-game test has been run as part of this change.
