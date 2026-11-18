# Build Timelapse — Prototype

Modular Java prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Implemented prototype foundations
- Project/session lifecycle and local project metadata persistence with validated project IDs, bounded collection reads and atomic file replacement.
- Timeline marker, multi-track and clip persistence (including clip speed); format 2 remains backward-readable for format 1 projects.
- Independent camera runtime contract, free/orbit/follow/fixed/path/drone modes, validated poses, orbit rig, named presets, timed waypoints, holds, easing and time-based path interpolation.
- Local versioned camera-path persistence through CameraPathStore / FileCameraPathStore, with Camera Editor save/load controls and path-ID validation.
- Tick-driven interval screenshot scheduler with a bounded background queue, per-frame settings snapshots, failure callbacks and optional local CSV capture-manifest wiring through the BuildTimelapse composition root.
- MinecraftScreenshotCaptureBackend marshals framebuffer work through MinecraftRenderThreadExecutor and restores the previous HUD/hand visibility in finally.
- Build place/remove event model, activity tracker, build-area bounds and placement milestones.
- Cinematic effects profile, fades, speed ramps and time/weather/HUD adapter contract.
- MP4/GIF/image-sequence settings, export queue and external FFmpeg encoder with MP4 dimension validation.
- Minecraft 1.8.9 Studio GUI with separate camera, timeline and capture-settings screens; these screens no longer pause the singleplayer world.
- Camera editor controls for pose, mode and timed path waypoints; timeline editor supports manual markers and track creation.
- Forge client-tick handler that advances camera paths and schedules interval captures.
- Supabase placeholder only; no network, upload, telemetry or authentication.

## Required runtime adapters / integration
1. Implement MinecraftCameraBridge with a client-only camera entity or Mixin. It must preserve/restore Minecraft's original render-view entity on every exit path. Never move the player or alter player motion/input.
2. Implement MinecraftScreenshotBridge using Minecraft 1.8.9 HUD/render settings and framebuffer/ScreenShotHelper APIs, then inject it into MinecraftScreenshotCaptureBackend with MinecraftRenderThreadExecutor.
3. Supply a local CaptureManifest to the BuildTimelapse composition root to attach frame and failure metadata logging to CaptureService.
4. Register BuildTimelapseClientTickHandler on the Forge event bus while the studio is active, and unregister it when the studio shuts down.
5. Connect Forge block place/break events; ignore cancelled, duplicate and remote-world changes.
6. Register/open BuildTimelapseStudioScreen from the intended client-side entry point and inject the configured project services.
7. Effects profiles and export jobs still need project-level persistence.
8. The timeline editor currently creates tracks and manual markers; detailed clip editing, trimming and drag/drop are not implemented.
9. Complete GIF palette optimization, optional audio muxing, export cancellation/resume, disk retention and crash recovery.
10. Add module registration, keybinds and optional Supabase/cloud features only during intended integration.

## Limitations
- Camera activation deliberately fails until a safe camera bridge is provided; recording does not silently fall back to moving or controlling the player.
- The screenshot backend provides render-thread dispatch and state restoration, but the low-level Minecraft framebuffer bridge still has to be implemented and injected.
- Camera paths can now be saved and loaded locally from the Camera Editor; this does not implement the actual Minecraft camera runtime.
- FFmpeg must be installed separately; no binary is bundled. MP4 is silent and GIF encoding is a basic path.
- World time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No Gradle wrapper/build configuration is present in this repository snapshot; no repository-native build was run.
