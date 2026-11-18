# Build Timelapse — Prototype

Modular Java prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Implemented prototype foundations
- Project/session lifecycle and local project metadata persistence.
- Timeline marker, multi-track and clip persistence (including clip speed); format 2 remains backward-readable for format 1 projects.
- Independent camera runtime contract, free/orbit/follow/fixed/path/drone modes, poses, orbit rig, named presets, timed waypoints, holds, easing and time-based path interpolation.
- Tick-driven interval screenshot scheduler with a bounded background queue, per-frame settings snapshots, failure callbacks and a local CSV capture-manifest listener.
- Build place/remove event model, activity tracker, build-area bounds and placement milestones.
- Cinematic effects profile, fades, speed ramps and time/weather/HUD adapter contract.
- MP4/GIF/image-sequence settings, export queue and external FFmpeg encoder.
- Minecraft 1.8.9 Studio GUI with separate camera, timeline and capture-settings screens.
- Camera editor controls for pose, mode and timed path waypoints; timeline editor supports manual markers and track creation.
- Forge client-tick handler that advances camera paths and schedules interval captures.
- Supabase placeholder only; no network, upload, telemetry or authentication.

## Required runtime adapters / integration
1. Implement `MinecraftCameraBridge` with a client-only camera entity or Mixin. It must preserve/restore Minecraft's original render-view entity on every exit path. Never move the player or alter player motion/input.
2. Implement `MinecraftScreenshotBridge` on Minecraft's render thread. The capture worker must marshal framebuffer work onto the render thread and restore HUD/hand visibility in a finally block. The bounded queue reports skipped frames when overloaded rather than allowing unbounded memory growth.
3. Attach a `CaptureManifest` to `CaptureService.setListener(...)` to persist frame and failure metadata as local CSV.
4. Register `BuildTimelapseClientTickHandler` on the Forge event bus while the studio is active, and unregister it when the studio shuts down.
5. Connect Forge block place/break events; ignore cancelled, duplicate and remote-world changes.
6. Register/open `BuildTimelapseStudioScreen` from the intended client-side entry point and inject the configured project services. The timeline editor currently creates tracks and manual markers; detailed clip editing and drag/drop are not implemented.
7. Persist camera paths, effects and export jobs; project metadata and timeline markers/tracks/clips are persisted, while frame PNGs and the CSV manifest are separate files.
8. Complete GIF palette optimization, optional audio muxing, export cancellation/resume, disk retention and crash recovery.
9. Add module registration, keybinds, settings and optional Supabase/cloud features only during intended integration.

## Limitations
- Camera activation deliberately fails until a safe camera bridge is provided; recording does not silently fall back to moving or controlling the player.
- Capture requires a Minecraft screenshot/framebuffer adapter; no Minecraft API is called directly from the capture worker.
- FFmpeg must be installed separately; no binary is bundled. MP4 is silent and GIF encoding is a basic path.
- World time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No Gradle wrapper/build configuration is present in this repository snapshot; no repository-native build was run.

