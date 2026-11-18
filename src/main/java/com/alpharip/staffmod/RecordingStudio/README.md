# Recording Studio — Standalone Module

Independent general-purpose Minecraft 1.8.9 / Forge recording module.

## Included
- Standalone Recording Studio project model with local metadata persistence.
- Manual recording controller and independent Forge runtime.
- Minecraft framebuffer capture backend writing ordered PNG frames.
- Start, pause, resume, stop and save controls in a dedicated Studio screen.
- Shared general-purpose timeline, marker and clip/track models.
- A feature-neutral timeline editor owned by Recording Studio.
- Build Timelapse consumes the shared Recording Studio timeline model and editor; the dependency is one-way.
- No Build Timelapse imports, build-event listeners, Supabase calls or network services inside Recording Studio.

## Current limitations
- Camera-path editing, effects and video/GIF export have not yet been migrated.
- Capture uses native framebuffer resolution and performs readback and PNG writing synchronously on the client thread.
- Audio, crash recovery and disk retention are not included yet.
- Client entry-point/module/keybind registration is intentionally unchanged.
- Build Timelapse still owns its build-specific camera, capture, effects and export implementations; these are next migration stages.
- No build configuration is present in this repository snapshot; compilation and in-game behavior have not been verified.
