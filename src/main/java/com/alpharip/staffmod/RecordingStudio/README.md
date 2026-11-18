# Recording Studio — Standalone Module

Independent general-purpose Minecraft 1.8.9 / Forge recording module.

## Included
- Standalone Recording Studio project model with local metadata persistence.
- Manual recording controller and independent Forge runtime.
- Minecraft framebuffer capture backend writing ordered PNG frames.
- Start, pause, resume, stop and save controls in a dedicated Studio screen.
- Loaded projects do not automatically resume a recording.
- No Build Timelapse imports, build-event listeners, Supabase calls or network services.

## Current limitations
- This is the independent manual frame-capture foundation, not yet feature parity with the existing Build Timelapse editing pipeline.
- Capture uses native Minecraft framebuffer resolution and performs readback and PNG writing synchronously on the client thread.
- Audio, timeline editing, camera-path editing, effects, video/GIF encoding, export queue UI, crash recovery and disk retention are not included yet.
- The client entry point must construct RecordingStudioRuntime with a RecordingStudioController and call open(Minecraft.getMinecraft()); module/keybind registration is intentionally not changed here.
- The existing Build Timelapse implementation is unchanged and remains separate.
- No build configuration is present in this repository snapshot; compilation and in-game behavior have not been verified.
