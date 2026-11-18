# Recording Studio — Standalone Module

Independent general-purpose Minecraft 1.8.9 / Forge recording module.

## Included
- Standalone Recording Studio project model with local metadata persistence.
- Manual recording controller and independent Forge runtime.
- Minecraft framebuffer capture backend writing ordered PNG frames.
- Start, pause, resume, stop and save controls in a dedicated Studio screen.
- Shared general-purpose timeline, marker and clip/track models and editor.
- Shared camera pose, modes, paths, waypoints, interpolation, presets, orbit calculations and local path persistence.
- Recording Studio-owned Minecraft render-view camera adapter. It uses an unspawned render-view entity and does not move or control the real player.
- In-game Camera Editor for activation, deactivation, position, rotation, FOV and camera paths.
- World-space camera marker: a gold wireframe camera body and cyan direction/frustum guide are rendered at the active camera position, including while the editor GUI is open.
- Camera paths are stored locally under the Recording Studio data directory.
- No Build Timelapse imports, build-event listeners, Supabase calls or network services inside Recording Studio.

## Current limitations
- The in-game marker is a debug visualization, not a full 3D camera model.
- Camera roll is not applied by the Minecraft render-view adapter.
- Camera path interpolation and render-thread behavior have not been compiled or tested in Minecraft.
- Capture uses native framebuffer resolution and performs readback and PNG writing synchronously on the client thread.
- Audio, crash recovery, disk retention, effects and video/GIF export migration are not included yet.
- Registered in the AlphaClient Forge entry point as the `/recordingstudio` client command; no keybind or module-menu entry has been added.
- No build configuration is present in this repository snapshot; compilation and in-game behavior have not been verified.
