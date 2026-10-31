# Block Placement Preview

A client-side preview for the block placement position while the player is holding a block and aiming at a valid placement target.

## Planned behavior

- Show a translucent preview of the held block at the position Minecraft would place it.
- Optionally indicate whether the preview intersects an existing block or other collision shape.
- Let the player adjust preview opacity.
- Hide the preview when the player is not holding a placeable block, has no valid target, or the feature is disabled.

## Current status

This folder is reserved for the feature implementation. The idea scaffold in `staffops/ideas/BlockPlacementPreview.java` currently contains only enabled, collision-display, and opacity settings. Rendering and interaction with Minecraft placement rules have not been implemented here yet.
