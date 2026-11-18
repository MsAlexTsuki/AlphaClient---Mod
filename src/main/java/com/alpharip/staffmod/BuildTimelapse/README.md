# Build Timelapse — Prototype

Modular Java prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Implemented prototype foundations
- Project/session lifecycle and local project metadata persistence with validated project IDs, bounded collection reads and atomic file replacement.
- Timeline marker, multi-track and clip persistence (including clip speed); format 2 remains backward-readable for format 1 projects.
- Independent camera runtime contract plus a client-only render-view camera entity; it is not inserted into the world and does not modify player position, rotation, motion or input. Free/orbit/follow/fixed/path/drone modes, validated poses, orbit rig, named presets, timed waypoints, holds, easing and time-based path interpolation.
- Local versioned camera-path persistence through CameraPathStore / FileCameraPathStore, with Camera Editor save/load controls and path-ID validation.
- Tick-driven interval screenshot scheduler with a bounded background queue, per-frame settings snapshots, failure callbacks and optional local CSV capture-manifest wiring through the BuildTimelapse composition root.
- MinecraftScreenshotCaptureBackend marshals framebuffer work through MinecraftRenderThreadExecutor and restores the previous HUD/hand visibility in finally.
- Concrete Minecraft 1.8.9 framebuffer adapter using the main framebuffer, OpenGL pixel readback and PNG output at native display resolution.
- Build place/remove event model, activity tracker, build-area bounds and placement milestones.
- Cinematic effects profile, fades, speed ramps and time/weather/HUD adapter contract.
- MP4/GIF/image-sequence settings, export queue and external FFmpeg encoder with MP4 dimension validation.
- Minecraft 1.8.9 Studio GUI with separate camera, timeline and capture-settings screens; these screens no longer pause the singleplayer world.
- Camera editor controls for pose, mode and timed path waypoints; timeline editor supports manual markers and track creation.
- Forge client-tick handler that advances camera paths and schedules interval captures; BuildTimelapseRuntime owns tick/block event registration and session shutdown.
- Supabase placeholder only; no network, upload, telemetry or authentication.

## Required runtime adapters / integration
1. The client-only camera bridge swaps Minecraft's render-view entity to an unspawned camera entity and restores the original render-view entity on shutdown. In-game validation is still required, including world transitions and failure cleanup.
2. Construct MinecraftFramebufferScreenshotBridge with the active Minecraft instance, then inject it into MinecraftScreenshotCaptureBackend with MinecraftRenderThreadExecutor.
3. Frame capture currently supports native display resolution. Custom output dimensions require a scaling/render-target implementation.
4. HUD visibility is controlled through GameSettings.hideGUI. Hiding the first-person hand is not implemented; requesting it fails explicitly until a dedicated renderer hook is added.
5. Supply a local CaptureManifest to the BuildTimelapse composition root to attach frame and failure metadata logging to CaptureService.
6. BuildTimelapseRuntime registers/unregisters the client tick handler and Forge block-event bridge for the studio session. The client entry point must construct the runtime and call openStudio(...).
7. ForgeMinecraftBlockEventBridge records player block placements, multi-place snapshots and breaks from non-remote integrated-world events; cancelled events are ignored.
8. Register/open BuildTimelapseStudioScreen from the intended client-side entry point and inject the configured project services.
9. Effects profiles and export jobs still need project-level persistence.
10. The timeline editor currently creates tracks and manual markers; detailed clip editing, trimming and drag/drop are not implemented.
11. Complete GIF palette optimization, optional audio muxing, export cancellation/resume, disk retention and crash recovery.
12. Add module registration, keybinds and optional Supabase/cloud features only during intended integration.

## Limitations
- Camera activation fails when the client world is unavailable. The bridge does not silently fall back to moving or controlling the player.
- The framebuffer adapter must run on the Minecraft client/render thread and captures the most recently rendered main framebuffer.
- Camera roll is not yet applied by the Minecraft renderer; it requires a dedicated render hook. Camera FOV is applied temporarily and the user's previous FOV is restored when the camera is deactivated.
- FFmpeg must be installed separately; no binary is bundled. MP4 is silent and GIF encoding is a basic path.
- World time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No Gradle wrapper/build configuration is present in this repository snapshot; no repository-native build was run.
