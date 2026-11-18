# Alpha Recording Studio

Standalone Minecraft 1.8.9 / Forge sister mod containing the Recording Studio and Build Timelapse recording code.

## Included code

- The complete `RecordingStudio` package: project storage, capture, microphone audio, camera, timeline, scenes, export, UI and in-game command.
- The complete `BuildTimelapse` package: build activity tracking, capture, camera paths, effects, export, timeline, UI and the Recording Studio adapter.
- Shared camera and timeline types used by both recording systems.
- A separate Forge mod entry point and mod metadata for `alpharecordingstudio`.

## Launch

Install the Alpha Recording Studio mod for Minecraft 1.8.9 Forge and use `/recordingstudio` inside a loaded world.

## Build

Use the included Gradle wrapper with the ForgeGradle 2.1 build configuration.

## Notes

- This branch contains only the Recording Studio and Build Timelapse source packages plus the standalone mod/build files.
- No AlphaClient source, launcher source, Supabase project files or unrelated client modules are included.
- Build Timelapse's existing UI/runtime composition has been retained as source; its independent launch/initialization path still requires verification in this standalone mod.
- FFmpeg and FFprobe are external requirements for the applicable export workflows.
- This extraction has not yet been compiled or tested in Minecraft.
