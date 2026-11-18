# Build Timelapse — Prototype

Modular Java prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Implemented prototype foundations
- Project/session lifecycle and local project metadata persistence with validated project IDs, bounded collection reads and atomic file replacement.
- Timeline marker, multi-track and clip persistence (including clip speed); the active editor uses Recording Studio's shared timeline models.
- Local versioned effects-profile persistence per project.
- Local versioned export-job metadata persistence per project; loading metadata does not automatically execute jobs.
- Build-specific block tracking, activity analytics, build-area bounds and milestones.
- Camera pose, path, waypoint, interpolation, preset, orbit and controller contracts are now defined in Recording Studio; Build Timelapse controllers and its Minecraft camera bridge consume those shared contracts.
- Tick-driven interval screenshot scheduler, bounded background queue, local capture manifest option and native-resolution Minecraft framebuffer PNG adapter.
- Cinematic effects profile, fades, speed ramps and time/weather/HUD adapter contract.
- MP4/GIF/image-sequence settings and FFmpeg encoder; GIF export uses a two-pass palette generation and palette-use workflow; export queue supports cancellation.
- Minecraft 1.8.9 Studio GUI and Forge client runtime lifecycle.
- Supabase placeholder only; no network, upload, telemetry or authentication.

## Remaining integration / feature work
1. Recording Studio does not yet own the camera runtime lifecycle, camera editor screen or camera-path UI/storage flow; these remain connected through the Build Timelapse UI.
2. Camera path persistence is still stored under the legacy Build Timelapse directory; migration to Recording Studio storage is pending.
3. Construct and inject the Minecraft framebuffer adapter in the client-side composition root.
4. Custom capture resolutions require a scaling/render-target implementation.
5. First-person hand hiding and camera roll require dedicated renderer hooks.
6. Client entry point must construct the runtime and connect module/keybind registration; this remains intentionally untouched.
7. Export retry/resume, crash recovery, audio muxing, disk retention and export progress UI are not implemented.
8. In-game camera, capture, cancellation and timeline GUI validation remains outstanding.

## Limitations
- Framebuffer access must run on the Minecraft client/render thread and currently supports native display resolution.
- The independent camera does not move/control the real player; hand hiding and camera roll are unsupported.
- FFmpeg must be installed separately; MP4 is silent and GIF encoding remains basic.
- World time/weather/HUD effects require an adapter that snapshots and restores original settings.
- No startup/menu/module/keybind registration or Alpha Launcher changes.
- No build configuration is present in this repository snapshot; no repository-native build was run.
