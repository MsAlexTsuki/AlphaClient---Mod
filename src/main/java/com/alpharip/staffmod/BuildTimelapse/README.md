# Build Timelapse — Alpha Recording Studio Addon Module

Modular Build Timelapse module packaged inside the optional Alpha Recording Studio addon for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Implemented prototype foundations
- Project/session lifecycle and local project metadata persistence with validated project IDs, bounded collection reads and atomic file replacement.
- Timeline marker, multi-track and clip persistence (including clip speed); the active editor uses Recording Studio's shared timeline models.
- Local versioned effects-profile persistence per project.
- Local versioned export-job metadata persistence per project; loading metadata does not automatically execute jobs.
- Build-specific block tracking, activity analytics, build-area bounds and milestones.
- Camera pose, path, waypoint, interpolation, preset, orbit and controller contracts are now defined in Recording Studio; Build Timelapse controllers and its Minecraft camera bridge consume those shared contracts.
- Tick-driven interval screenshot scheduler, bounded background queue, local capture manifest option and native-resolution Minecraft framebuffer PNG adapter. Capture waits for the correct render event: after world rendering when the HUD is hidden, or after the HUD when it is visible; pending captures are cancelled on world unload.
- Optional Recording Studio module adapter: the Module panel can list saved Build Timelapse camera paths, position the shared camera at the first waypoint, and let Recording Studio drive path playback while capturing the framebuffer.
- Cinematic effects profile, fades and speed ramps, with a Minecraft time/weather/HUD adapter that snapshots and restores the values it changes.
- MP4/GIF/image-sequence settings and FFmpeg encoder; GIF export uses a two-pass palette generation and palette-use workflow; MP4/GIF output is written to an extension-preserving temporary file and atomically replaces the final output only after successful encoding; export queue supports cancellation.
- Minecraft 1.8.9 Studio GUI and Forge client runtime lifecycle; the addon composition root initializes it on client-world load and registers its camera-path adapter with Recording Studio.
- `/buildtimelapse` command to open the workspace and create, save and edit camera paths.
- Supabase placeholder only; no network, upload, telemetry or authentication.

## Remaining integration / feature work
1. Camera path persistence remains under `buildtimelapse/camera-paths`; Recording Studio reads the existing saved paths without migrating or duplicating them.
2. Custom capture resolutions require a scaling/render-target implementation.
3. First-person hand hiding and camera roll require dedicated renderer hooks.
4. The Build Timelapse workspace currently opens through `/buildtimelapse`; no keybind or top-level module-menu entry is registered.
5. Export retry/resume, crash recovery, audio muxing, disk retention and export progress UI are not implemented.
6. In-game camera, capture, cancellation and timeline GUI validation remains outstanding.

## Limitations
- Framebuffer access must run on the Minecraft client/render thread and currently supports native display resolution. The adapter restores the previous framebuffer binding and viewport; only pixel readback runs on the client thread, while image conversion and atomic PNG disk writes run on the capture worker. Unsupported hand-hiding and custom-resolution controls are disabled in the capture settings screen.
- The independent camera does not move/control the real player; hand hiding and camera roll are unsupported. The render-view bridge restores the previous view only while it still owns the render-view entity, and preserves a later external FOV change.
- FFmpeg must be installed separately; MP4 is silent and GIF encoding remains basic.
- A Minecraft effects adapter now snapshots and restores world time, weather and HUD visibility. The current Build Timelapse screen does not yet expose effect controls or automatically apply a saved effects profile.
- No custom main-menu or keybind entry is registered. The addon Forge entry point, world-load runtime initialization, Recording Studio module registration and `/buildtimelapse` command are present. Alpha Launcher is not changed.
- The isolated ForgeGradle 2.1 build configuration and wrapper are present. No repository-native build or in-game validation has been run.
