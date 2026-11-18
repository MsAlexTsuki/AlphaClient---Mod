# Recording Studio — Standalone Module

Independent general-purpose Minecraft 1.8.9 / Forge recording module.

## Included
- Standalone Recording Studio project model with local metadata persistence.
- Manual recording controller and independent Forge runtime.
- Minecraft framebuffer capture backend writing ordered PNG frames.
- Start, pause, resume, stop and save controls in a dedicated Studio screen.
- Shared general-purpose timeline, marker and clip/track models.
- Feature-neutral timeline editor owned by Recording Studio.
- Shared camera contracts: camera pose, modes, paths, waypoints, interpolation, presets, orbit calculations, runtime port and local path persistence.
- Build Timelapse now consumes the shared camera contracts rather than its former package-local camera model classes.
- No Build Timelapse imports, build-event listeners, Supabase calls or network services inside Recording Studio.

## Current limitations
- Recording Studio's camera controller is not yet connected to its runtime, GUI or Minecraft camera adapter. The camera editor and active camera lifecycle are still exposed through Build Timelapse.
- Camera path storage migration to a Recording Studio-owned directory is pending.
- Effects and video/GIF export have not yet been migrated.
- Capture uses native framebuffer resolution and performs readback and PNG writing synchronously on the client thread.
- Audio, crash recovery and disk retention are not included yet.
- Client entry-point/module/keybind registration is intentionally unchanged.
- No build configuration is present in this repository snapshot; compilation and in-game behavior have not been verified.
