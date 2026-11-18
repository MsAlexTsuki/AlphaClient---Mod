# Recording Studio — Standalone Module

Independent general-purpose Minecraft 1.8.9 / Forge recording module.

## Included
- Standalone Recording Studio project model with local metadata persistence.
- Local project catalog with list, create and open commands (`/recordingstudio list`, `/recordingstudio create <name>`, `/recordingstudio open <project-id>`).
- Atomic timeline persistence for tracks, clips, playback speeds and timeline markers; saved projects can restore their timeline after reopening.
- Project metadata and timeline are saved from the Studio Save Project control and automatically on Studio shutdown/world unload; recording start, pause and resume also create recovery checkpoints.
- An atomic per-project active-session marker is retained while a recording is in progress and cleared only after a successful final checkpoint and completion of all accepted frame and microphone-audio writes; reopening a project with a leftover marker reports an interrupted session while preserving existing frame files.
- Stopping a session keeps its recovery marker while queued PNG writes are pending; the marker is cleared after the queue drains, including through a background finalizer during Studio shutdown.
- Existing frame filenames are scanned before recording resumes so a reopened project appends frames instead of overwriting earlier captures.
- Manual recording controller and independent Forge runtime.
- Explicit microphone recording controls; PCM 44.1 kHz, 16-bit mono WAV files are written on a dedicated audio thread.
- Completed microphone recordings are added as clips to the standalone Audio timeline track and persisted with the project.
- Background MP4 and GIF export from ordered captured PNG frames, with cancellation and temporary-output cleanup.
- MP4 export includes saved Audio timeline clips, delayed to their timeline positions and mixed when multiple clips exist; GIF output is silent.
- Microphone capture stops when video recording is paused or stopped; a new audio clip can be started after video recording resumes.
- Minecraft framebuffer capture backend reading OpenGL pixels on the client thread and converting/writing PNG frames asynchronously on a dedicated worker.
- Bounded encoder queue (one active write plus up to eight queued frames) with dropped-frame accounting when the queue is full.
- Atomic PNG file replacement so interrupted writes do not leave partially written final frame files.
- Live pending-write counter; accepted frame writes drain without blocking the Minecraft client thread during shutdown.
- Start, pause, resume, stop and save controls in a dedicated Studio screen.
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
- The in-game marker is a debug visualization, not a full 3D camera model.
- Camera roll is not applied by the Minecraft render-view adapter.
- Camera path interpolation and render-thread behavior have not been compiled or tested in Minecraft.
- Capture uses native framebuffer resolution. OpenGL framebuffer readback remains on the Minecraft client thread; PNG conversion and disk writing run asynchronously.
- FFmpeg must be installed separately and available as `ffmpeg` on PATH for MP4/GIF export.
- Export derives its nominal FPS from the capture interval and uses captured native resolution (MP4 is padded to even dimensions). Changing the capture interval during a session can make audio synchronization approximate; custom export settings, percentage progress and GIF audio are not included yet.
- Disk retention and effects/filters are not included yet.
- Audio capture uses the system's default microphone input. Device selection, live monitoring and automatic microphone restart after a video pause are not included yet.
- Crash recovery is currently checkpoint-based: it detects an uncleanly interrupted session and preserves previously written frames for continuation; it does not reconstruct a frame that was still being captured or encoded at the moment of the crash.
- Registered in the AlphaClient Forge entry point as the `/recordingstudio` client command; no keybind or module-menu entry has been added.
- No build configuration is present in this repository snapshot; compilation and in-game behavior have not been verified.
