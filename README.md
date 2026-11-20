# Alpha Recording Studio

Alpha Recording Studio is an optional sister mod (addon) for Alpha Client on Minecraft 1.8.9 / Forge.

**Alpha Client is required. Alpha Client does not require Alpha Recording Studio.** Players can run Alpha Client without this addon; installing the addon adds the Recording Studio and Build Timelapse recording tools.

## Included code

- The complete `RecordingStudio` package: project storage, capture, microphone audio, Windows WASAPI game-audio loopback, camera, timeline, scenes, export, UI and in-game command.
- The complete `BuildTimelapse` package: build activity tracking, capture, camera paths, effects, export, timeline, UI and the Recording Studio adapter. Its runtime and adapter are initialized automatically when a client world loads.
- Shared camera and timeline types used by both recording systems.
- A dedicated Forge addon entry point and mod metadata. Forge declares `alphaclient` as a required dependency of `alpharecordingstudio`.

## Installation and launch

Install Alpha Client first, then install Alpha Recording Studio alongside it in the same Minecraft 1.8.9 Forge instance. Forge will not load Alpha Recording Studio when Alpha Client is absent.

Use `/recordingstudio` inside a loaded world to open Recording Studio. Use `/buildtimelapse` to open the Build Timelapse workspace and create or edit camera paths for the Recording Studio module.

## Build

Use the included Gradle wrapper with the ForgeGradle 2.1 build configuration.

## Notes

- This branch contains the Recording Studio and Build Timelapse source packages plus the addon build files.
- No AlphaClient core source, launcher source, Supabase project files or unrelated client modules are included.
- Build Timelapse is composed by the addon at client-world load. Its camera-path adapter is registered before Recording Studio opens; `/buildtimelapse` opens the separate workspace.
- Windows x64 Game Audio uses the packaged WASAPI loopback bridge and captures the Windows default output mix from all applications, not Minecraft alone; Stereo Mix and virtual audio cables are not required. The microphone remains a separate input.
- Building the Windows addon JAR requires a Java 8 JDK, CMake 3.15+ and the Visual Studio C++ x64 build tools so Gradle can compile and package `alpha_wasapi.dll`.
- FFmpeg and FFprobe are external requirements for the applicable export workflows.
- The addon dependency metadata, runtime composition and source changes have not yet been compiled or tested in Minecraft.
