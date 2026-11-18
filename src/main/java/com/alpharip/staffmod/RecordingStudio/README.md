# Recording Studio — Standalone Module

Independent general-purpose Minecraft 1.8.9 / Forge recording module.

## Included
- Standalone Recording Studio project model with local metadata persistence.
- Local project catalog with list, create and open commands (`/recordingstudio list`, `/recordingstudio create <name>`, `/recordingstudio open <project-id>`).
- Atomic timeline persistence for tracks, clips, playback speeds and timeline markers; saved projects can restore their timeline after reopening.
- Project metadata and timeline are saved from the Studio Save Project control and automatically on Studio shutdown/world unload; recording start, pause and resume also create recovery checkpoints.
- Project metadata preserves elapsed active recording time (excluding pauses) and the capture interval; active sessions checkpoint elapsed time once per second so crash recovery restores the latest saved active duration without counting application downtime.
- An atomic per-project active-session marker is retained while a recording is in progress and cleared only after a successful final checkpoint and completion of all accepted frame and microphone-audio writes; reopening a project with a leftover marker reports an interrupted session while preserving existing frame files.
- Stopping a session keeps its recovery marker while frame or microphone writes are pending; the marker is cleared after finalization, including through a background finalizer during Studio shutdown.
- Existing frame filenames are scanned before recording resumes so a reopened project appends frames instead of overwriting earlier captures.
- Manual recording controller and independent Forge runtime.
- OBS-inspired AlphaClient workspace with a live Minecraft viewport and dedicated Scenes, Sources, Module, Audio Mixer, Controls and Timeline panels.
- Persisted scene management with create, rename, remove and cycle controls; the final remaining scene is protected from deletion.
- Built-in per-scene Minecraft Capture and Microphone sources with enable/disable, remove and reorder controls. Select a source in the Sources panel, then move it up or down; the order is saved with the scene. Disabling or removing microphone capture stops an active microphone session. Scene selection, scene edits and Game Capture source changes are locked during active or paused recording so captured frame timing cannot silently become discontinuous.
- Module panel with Normal Gameplay and Build Timelapse choices; optional module adapters can expose additional recording workflows without adding feature-module imports to Recording Studio.
- Build Timelapse adapter exposes saved Build Timelapse camera paths in the Module panel. Selecting a path positions the shared Build Timelapse camera at its first waypoint; recording starts path playback and captures the moving camera view. Pausing and resuming the recording also pauses and resumes that path.
- Explicit microphone recording controls with selectable detected input devices or the operating-system default; the selection is saved per project. PCM 44.1 kHz, 16-bit mono WAV files are written on a dedicated audio thread.
- Completed microphone recordings are added as clips to the standalone Audio timeline track and persisted with the project.
- Stopped frame recordings are added as an editable `@capture` clip on the Video timeline track; the clip duration follows the active recording time.
- Background MP4 and GIF export from ordered captured PNG frames, with cancellation and temporary-output cleanup; files are stored under `recordingstudio/exports/<project-id>/`.
- YouTube-oriented export controls: Original, 1080p and 720p resolution limits (downscale only, preserve aspect ratio); MP4 High/Balanced/Smaller file quality presets; source frame rate retained; H.264 High Profile MP4 with AAC audio resampled to 48 kHz.
- MP4 export includes saved Audio timeline clips, delayed to their timeline positions and mixed when multiple clips exist; GIF output is silent.
- Microphone capture is split into separate timeline clips when video recording is paused; if microphone capture was active before the pause, a new microphone clip starts automatically after video recording resumes and the previous WAV file has finished finalizing. Manually stopped microphone capture is not restarted.
- Minecraft framebuffer capture backend scheduled at the end of the HUD render pass, before the current GuiScreen is drawn, so the Recording Studio window itself is excluded from gameplay footage. When Minecraft's hide-GUI setting suppresses overlay events, capture falls back to the world-render event. OpenGL framebuffer readback runs on the client thread; the previous framebuffer binding and viewport are restored immediately, while PNG conversion and disk writes run asynchronously on a dedicated worker.
- Bounded encoder queue (one active write plus up to eight queued frames) with dropped-frame accounting when the queue is full.
- Atomic PNG file replacement so interrupted writes do not leave partially written final frame files.
- Live pending-write counter; accepted frame writes drain without blocking the Minecraft client thread during shutdown.
- Start, pause, resume, stop and save controls in a dedicated Studio screen.
- AlphaClient dark-and-gold styling across the main dashboard, timeline editor, camera editor and storage overview, with consistent custom buttons and panel layout.
- Read-only storage overview for project metadata, captured video frames, microphone audio and exported media; users can refresh the scan without changing or deleting files.
- Adjustable capture interval in 100 ms steps, bounded to 100–60000 ms.
- Separate successful-frame and failed-frame counters; failed writes do not inflate the saved-frame count.
- Shared general-purpose timeline, marker and clip/track models and editor.
- Shared camera pose, modes, paths, waypoints, interpolation, presets, orbit calculations and local path persistence.
- Recording Studio-owned Minecraft render-view camera adapter. It uses an unspawned render-view entity and does not move or control the real player.
- In-game Camera Editor for activation, deactivation, position, rotation, FOV and camera paths.
- World-space camera marker: a gold wireframe camera body and cyan direction/frustum guide are rendered at the active camera position, including while the editor GUI is open.
- Camera paths are stored locally under the Recording Studio data directory.
- No Build Timelapse imports, build-event listeners, Supabase calls or network services inside Recording Studio.

## Current limitations
- The Build Timelapse module option is registered when BuildTimelapseRuntime starts. Closing its Studio screen releases its camera but keeps the adapter registered until the current world unloads, so Recording Studio can select it in the same world session.
- The host/client entry point still needs to initialize BuildTimelapseRuntime; until then the Build Timelapse option reports that its runtime is not initialized.
- Build Timelapse camera paths remain stored under `buildtimelapse/camera-paths/`; the Recording Studio Module panel reads those saved paths. Path creation/editing remains in the Build Timelapse Camera Editor.
- If a separate Build Timelapse recording is already active, Recording Studio will not take ownership of its camera.
- The in-game marker is a debug visualization, not a full 3D camera model.
- Camera roll is not applied by the Minecraft render-view adapter.
- Camera path interpolation and render-thread behavior have not been compiled or tested in Minecraft.
- Capture uses native framebuffer resolution. OpenGL framebuffer readback remains on the Minecraft client thread; PNG conversion and disk writing run asynchronously.
- FFmpeg must be installed separately and available as `ffmpeg` on PATH for MP4/GIF export.
- Export derives its frame rate from the capture interval (including sub-1 FPS rates) and uses the first captured frame's resolution. Video-track clip start positions, durations and playback speeds are applied to MP4 and GIF exports; the reserved `@capture` source refers to the project's captured PNG sequence, while other video clip sources must be valid video file paths. At least one captured PNG frame is still required to establish the export canvas, so external video clips cannot yet be exported by themselves. Audio-track clip start positions, durations and playback speeds are applied during MP4 export; GIF output remains silent. MP4 and GIF exports report FFmpeg progress as a percentage in the Studio status line; GIF progress includes a short palette-generation stage. Export resolution can be set to Original, 1080p or 720p; resolution presets preserve the source aspect ratio and never upscale. MP4 quality presets are High (CRF 18), Balanced (CRF 21) and Smaller file (CRF 25), with High selected by default. MP4 uses H.264 High Profile, AAC audio at 48 kHz, and the source recording frame rate; the export settings are held for the current Recording Studio session and are not persisted between launches. Changing the capture interval between recording segments in the same project can make audio synchronization approximate; GIF output remains silent.
- Automatic disk retention, cleanup policies and effects/filters are not included; the storage screen is informational only and never deletes files.
- Microphone input can be selected from detected Java Sound capture devices or set to the system default; the selected device is saved with the project. The Refresh Mic control re-enumerates devices and switches to the system default if the saved input has been unplugged. Live microphone monitoring is not included. Game audio capture is not implemented.
- Crash recovery is currently checkpoint-based: it detects an uncleanly interrupted session and preserves previously written frames for continuation; it does not reconstruct a frame or microphone audio that was still being captured or encoded at the moment of the crash. Active-time recovery typically loses up to about one second since the last checkpoint under normal tick operation; a longer client stall can increase that gap.
- Scene sources currently cover the built-in Minecraft framebuffer and microphone inputs. Arbitrary media sources, filters and independent per-scene audio-device overrides are not implemented.
- The client entry point still needs to register the Recording Studio command and initialize the Build Timelapse runtime/module adapter; no keybind or top-level module-menu entry has been added.
- The mod-ideas branch contains an isolated ForgeGradle build setup, but the current Recording Studio changes have not been compiled or verified in-game.
