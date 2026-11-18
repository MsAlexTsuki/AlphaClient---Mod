# Build Timelapse — Prototype

Modular Java 8 prototype foundation for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Included
- Project/session lifecycle, local project metadata and timeline-marker persistence.
- Independent camera runtime contract, camera modes (free/orbit/follow/fixed/path/drone), poses, orbit rig, named waypoints and smooth interpolation.
- Timed screenshot service with serial worker, interval/resolution/HUD preferences, capture callbacks and frame metadata.
- Timeline marker and duration model.
- Block place/remove event model, activity tracker and build-area bounds.
- Cinematic effects profile, fades, speed ramps, time/weather/HUD adapter contract.
- MP4/GIF/image-sequence export settings, queue and external FFmpeg encoder.
- Studio screen view model/controller foundation.
- Minecraft camera, screenshot and block-event integration interfaces.
- Supabase placeholder; no network, upload, telemetry or authentication.

## Integration still required
1. Implement `MinecraftCameraBridge` with a client-only camera entity or Mixin. Preserve and restore the original render-view entity on every exit path. Never move the player or alter player motion/input.
2. Implement `MinecraftScreenshotBridge` on Minecraft's render thread. Marshal framebuffer work from the capture worker to the render thread and restore HUD/hand visibility in a finally block.
3. Connect Forge block place/break events; ignore cancelled, duplicate and remote-world changes.
4. Build the actual Minecraft 1.8.9 `GuiScreen` around the view models; this package deliberately has no Minecraft/Forge imports.
5. Extend persistence for camera paths, full timeline tracks, capture manifests, effects and export jobs. Current store saves project metadata and timeline markers; `loadTimeline` restores marker data. PNG frames remain separate.
6. Complete GIF palette optimization, optional audio muxing, cancellation/resume, disk retention and crash recovery.
7. Add imports, module registration, keybinds, settings and optional Supabase/cloud features only during intended integration work.

## Limitations
- Camera is inert until a safe client camera bridge is implemented. Player coordinates/motion are not changed.
- Capture requires a Minecraft screenshot/framebuffer adapter; no Minecraft API is called from the worker thread.
- FFmpeg must be installed separately; no binary is bundled. MP4 is silent; GIF encoding is a basic path.
- World time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No Gradle wrapper/build configuration is present in this repository snapshot; no repository-native build was run.

