# AlphaClient — Mod Ideas

This branch is an isolated prototype workspace. It is not the production development branch.

## Working boundaries

- Recording Studio and related prototype work belongs on mod-ideas.
- Do not modify development or Alpha Launcher while working here.
- Do not copy additional project files from development into this branch.
- Keep prototype implementation and production integration separate.
- This branch targets Minecraft 1.8.9, Forge 11.15.1.2318 and Java 8.

## Recording Studio

Recording Studio is designed as a standalone manual recording workspace. It can use the current Minecraft view or an optional feature-module camera source.

### Implemented in this prototype

- OBS-inspired Recording Studio screen with recording controls, module selection, timeline entry point, audio controls, export controls and storage view.
- Normal Gameplay and Build Timelapse module choices.
- A module adapter contract so Recording Studio does not need to depend directly on each feature module.
- Build Timelapse camera-path discovery and playback integration through BuildTimelapseRecordingModuleAdapter.
- A camera preview HUD that:
  - is only drawn while a registered module or the standalone Studio camera reports an active external camera;
  - can be toggled with the HUD Preview control in Recording Studio;
  - can be moved by dragging its title bar;
  - can be resized by dragging its lower-right corner.
- The preview copies the current world framebuffer after world rendering. It does not render a second, independent Minecraft world view.

### Known limitations / not verified

- The preview is a copy of the currently rendered external-camera view. The current camera bridge changes Minecraft's global render-view entity; it does not preserve the player's normal view and render a second camera independently.
- Full scene/source creation and management are not implemented; the current screen still contains placeholder scene/source labels.
- Game audio capture is not implemented. The audio service currently handles microphone capture only.
- Timeline editing and export behavior still require further implementation and validation.
- No full Forge build or in-game test has been completed for the current prototype changes. Do not treat the preview as verified until it is compiled and tested in Minecraft.

## Integration checklist for development

Do not integrate this prototype automatically. When it is time to integrate, use this checklist and inspect the current development code before editing it.

1. **Bring in the source packages**
   - com.alpharip.staffmod.RecordingStudio
   - The required com.alpharip.staffmod.BuildTimelapse implementation and its integration adapter.
   - Preserve existing production classes and entrypoints. Do not replace the production AlphaClient entrypoint with a prototype entrypoint.
2. **Create the recording services in the production client lifecycle**
   - Construct a RecordingProject.
   - Provide a RecordingCaptureService.FrameWriter that captures the Minecraft framebuffer on the client thread and writes PNG frames.
   - Construct RecordingCaptureService and RecordingStudioController with the project's local storage directory and timeline.
   - Keep all OpenGL/framebuffer reads on the Minecraft client thread.
3. **Initialize the Build Timelapse adapter**
   - Create one BuildTimelapseRuntime for the loaded world.
   - Call BuildTimelapseRuntime.start() once after the client world is available. This registers the Build Timelapse camera-path adapter in RecordingStudioModuleRegistry.
   - Shut the runtime down when that world unloads. Do not create duplicate runtimes or duplicate Forge event registrations.
4. **Expose the Recording Studio entry point**
   - Create one RecordingStudioRuntime for the recording controller during the client/world lifecycle.
   - Open it through the intended AlphaClient command or UI entry point by calling RecordingStudioRuntime.open(Minecraft.getMinecraft()) while a world and player are loaded.
   - Ensure the runtime is shut down exactly once when the Studio session ends or the world unloads.
5. **Validate the integration**
   - Confirm the selected Build Timelapse path is listed and can be prepared.
   - Confirm the external camera state is reported by the module adapter.
   - Confirm the HUD preview appears only for an active external camera, can be toggled without stopping capture, and supports dragging/resizing.
   - Confirm normal gameplay recording does not show the external-camera preview.
   - Build with the production ForgeGradle setup and perform the required in-game checks.

### Integration items that must be resolved against development

The exact production entrypoint, command/UI registration point, project-storage path, and concrete Minecraft framebuffer writer must be identified from the current development branch during integration. They are intentionally not guessed or modified from this prototype branch.

## Launcher

Alpha Launcher is outside the scope of this branch. No launcher changes are required for the prototype.
