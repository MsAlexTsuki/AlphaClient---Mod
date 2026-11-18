# Recording Studio — Standalone Module

Independent general-purpose Minecraft 1.8.9 / Forge recording module.

## Included
- Standalone Recording Studio project model with local metadata persistence.
- Local project catalog with list, create and open commands (`/recordingstudio list`, `/recordingstudio create <name>`, `/recordingstudio open <project-id>`).
- Atomic timeline persistence for tracks, clips, playback speeds and timeline markers; saved projects can restore their timeline after reopening.
- Project metadata and timeline are saved from the Studio Save Project control and automatically on Studio shutdown/world unload.
- Existing frame filenames are scanned before recording resumes so a reopened project appends frames instead of overwriting earlier captures.
- Manual recording controller and independent Forge runtime.
- Minecraft framebuffer capture backend reading OpenGL pixels on the client thread and converting/writing PNG frames asynchronously on a dedicated worker.
- Bounded encoder queue (one active write plus up to eight queued frames) with dropped-frame accounting when the queue is full.
- Atomic PNG file replacement so interrupted writes do not leave partially written final frame files.
- Live pending-write counter; accepted frame writes drain without blocking the Minecraft client thread during shutdown.
- Start, pause, resume, stop and save controls in a dedicated Studio screen.
- Adjustable capture interval in 100 ms steps, bounded to 100–60000 ms.
- Separate successful-frame and failed-frame counters; failed writes do not inflate the saved-frame count.
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
- Capture uses native framebuffer resolution. OpenGL framebuffer readback remains on the Minecraft client thread; PNG conversion and disk writing run asynchronously.
- Audio, crash recovery, disk retention, effects and video/GIF export migration are not included yet.
- Registered in the AlphaClient Forge entry point as the `/recordingstudio` client command; no keybind or module-menu entry has been added.
- No build configuration is present in this repository snapshot; compilation and in-game behavior have not been verified.
