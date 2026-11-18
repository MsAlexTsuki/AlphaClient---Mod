# Build Timelapse — Prototype

Modular Java prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Implemented prototype foundations
- Project/session lifecycle and local project metadata persistence with validated project IDs, bounded collection reads and atomic file replacement.
- Timeline marker, multi-track and clip persistence (including clip speed); format 2 remains backward-readable for format 1 projects.
- Timeline track model supports adding/removing clips, moving clip start times, trimming start/duration and changing playback speed.
- Local versioned effects-profile persistence per project, with validated values and atomic file replacement.
- Local versioned export-job metadata persistence per project, preserving job IDs, source/output paths and export settings. Loading metadata does not automatically execute jobs.
- Independent camera runtime contract plus client-only render-view camera entity; it does not modify player movement or input. Free/orbit/follow/fixed/path/drone modes, validated poses, named presets, timed waypoints, easing and interpolation.
- Local versioned camera-path persistence and Camera Editor save/load controls.
- Tick-driven interval screenshot scheduler with bounded background queue, settings snapshots, failure callbacks and optional local capture manifest.
- Minecraft 1.8.9 framebuffer adapter using OpenGL readback and PNG output at native display resolution.
- Build place/remove event model, activity tracker, build-area bounds and placement milestones.
- Cinematic effects profile, fades, speed ramps and time/weather/HUD adapter contract.
- MP4/GIF/image-sequence settings, export queue and external FFmpeg encoder.
- Minecraft 1.8.9 Studio GUI with separate camera, timeline and capture-settings screens.
- Forge client-tick handler and runtime-owned event registration/session shutdown.
- Supabase placeholder only; no network, upload, telemetry or authentication.

## Remaining integration / feature work
1. Construct and inject the Minecraft framebuffer adapter in the client-side composition root.
2. Custom capture resolutions require a scaling/render-target implementation.
3. First-person hand hiding requires a dedicated renderer hook.
4. The client entry point must construct BuildTimelapseRuntime and call openStudio(...); module/keybind registration is not connected.
5. Timeline clip model operations exist, but GUI selection, clip creation, move/trim/speed controls and drag/drop are not implemented.
6. Export queue cancellation, resume/retry policy and crash recovery are not implemented.
7. Complete GIF palette optimization, optional audio muxing, disk retention and export progress UI.
8. Camera roll requires a render hook. In-game camera and framebuffer validation remains outstanding.

## Limitations
- Framebuffer access must run on the Minecraft client/render thread and currently supports native display resolution.
- Hand hiding is explicitly unsupported until a renderer hook exists.
- FFmpeg must be installed separately; MP4 is silent and GIF encoding remains basic.
- World time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No Gradle wrapper/build configuration is present in this repository snapshot; no repository-native build was run.
