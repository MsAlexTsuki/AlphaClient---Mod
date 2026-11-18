# Build Timelapse — Prototype

Java 8 modular foundation for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Included
- Project/session lifecycle and local metadata persistence.
- Independent cinematic camera contract, free/orbit/follow/fixed/path/drone modes, camera poses, orbit rig, waypoint paths and smooth interpolation.
- Timed screenshot service with serial worker, interval/resolution/HUD preferences, capture callbacks and frame metadata.
- Timeline markers and duration model.
- Block place/remove event model, activity tracker and build-area bounds.
- Cinematic effects profile, fades, speed ramps, time/weather/HUD adapter contract.
- MP4/GIF/image-sequence export settings, queue and external FFmpeg encoder.
- Studio screen view model and controller.
- Minecraft camera, screenshot and block event integration interfaces.
- Supabase placeholder; no network, upload, telemetry or authentication.

## Integration still required
1. Implement the camera bridge with a client-only camera entity or Mixin. Save and restore Minecraft's original render-view entity on every exit path. Never move the player or alter player motion/input.
2. Implement screenshot/framebuffer capture on Minecraft's render thread. The capture worker must marshal work to that thread; restore HUD/hand visibility in a finally block.
3. Connect Forge block place/break events and filter cancelled, duplicate and remote-world changes.
4. Build a real Minecraft 1.8.9 GuiScreen around the UI view models; this package intentionally has no Minecraft/Forge imports.
5. Extend persistence to save camera paths, full timeline tracks, capture manifests, effects and export jobs. Current store saves project metadata only; frame PNGs remain separate.
6. Complete GIF palette optimization, optional audio muxing, cancellation/resume, disk retention and crash recovery.
7. Add imports, module registration, keybinds, settings and optional Supabase/cloud features only during integration.

## Limitations
- Camera is inert until the safe client camera bridge exists. Player coordinates/motion are not changed by this prototype.
- Capture requires a Minecraft screenshot/framebuffer adapter.
- FFmpeg must be installed separately. MP4 export is silent; GIF encoding is a basic path.
- Time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No Gradle wrapper/build configuration exists in this repository snapshot; no repository-native build was run.

