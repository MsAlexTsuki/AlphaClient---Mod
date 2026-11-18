# Build Timelapse — Prototype

Modular Java prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Implemented prototype foundations
- Project/session lifecycle and local project metadata persistence with validated project IDs, bounded collection reads and atomic file replacement.
- Timeline marker, multi-track and clip persistence (including clip speed); timeline editor now supports track/clip selection, clip creation, moving, trimming, speed changes and removal.
- Local versioned effects-profile persistence per project.
- Local versioned export-job metadata persistence per project; loading metadata does not automatically execute jobs.
- Independent camera runtime, client-only render-view camera entity, camera paths and local path persistence.
- Tick-driven interval screenshot scheduler, bounded background queue, local capture manifest option and native-resolution Minecraft framebuffer PNG adapter.
- Build placement/removal event tracking, activity tracker, build-area bounds and milestones.
- Cinematic effects profile, fades, speed ramps and time/weather/HUD adapter contract.
- MP4/GIF/image-sequence settings and FFmpeg encoder; export queue supports cancellation by interrupting the active job and terminating FFmpeg.
- Minecraft 1.8.9 Studio GUI and Forge client runtime lifecycle.
- Supabase placeholder only; no network, upload, telemetry or authentication.

## Remaining integration / feature work
1. Construct and inject the Minecraft framebuffer adapter in the client-side composition root.
2. Custom capture resolutions require a scaling/render-target implementation.
3. First-person hand hiding and camera roll require dedicated renderer hooks.
4. The client entry point must construct BuildTimelapseRuntime and call openStudio(...); module/keybind registration is not connected.
5. Export retry/resume and crash recovery are not implemented.
6. Complete GIF palette optimization, optional audio muxing, disk retention and export progress UI.
7. In-game camera, capture, cancellation and timeline GUI validation remains outstanding.

## Limitations
- Framebuffer access must run on the Minecraft client/render thread and currently supports native display resolution.
- Hand hiding is unsupported until a renderer hook exists.
- FFmpeg must be installed separately; MP4 is silent and GIF encoding remains basic.
- World time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No Gradle wrapper/build configuration is present in this repository snapshot; no repository-native build was run.
