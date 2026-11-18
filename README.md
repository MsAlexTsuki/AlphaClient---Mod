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
- Basic scene management is implemented with create, rename, remove and selection controls. Built-in Minecraft Capture and Microphone sources can be enabled, disabled and removed per scene. Arbitrary file/device sources, source reordering and filters are still missing.
- Game audio capture is not implemented. The audio service handles microphone capture only; users can select a detected input device or use the system default, and the selection is stored per Recording Studio project.
- Video timeline clip start positions, durations and playback speeds are applied during MP4 and GIF export. The automatically created `@capture` clip references the recorded PNG sequence; additional video clips use file paths. At least one captured PNG frame is still required to establish the export canvas. Audio timeline clip timing and playback speed are applied during MP4 export.
- The prototype does not yet contain a verified production bootstrap for constructing and owning all recording services. The Minecraft framebuffer writer is implemented under `RecordingStudio/integration/`.
- No full Forge build or in-game test has been completed for the current prototype changes. Do not treat the preview as verified until it is compiled and tested in Minecraft.

## Integration checklist for development

Do not integrate this prototype automatically. These are the concrete integration tasks identified from the current source layout. The development branch itself remains unchanged.

### Production files that will need integration changes

1. `src/main/java/com/alpharip/alphaclient/AlphaClient.java`
   - Add lifecycle ownership for the Recording Studio controller/runtime and the Build Timelapse runtime.
   - Add world-load/world-unload handling so each runtime is created once per world and shut down once when that world unloads.
   - Add a client-thread helper that opens the Recording Studio runtime for the current world.
   - The existing `initialize(FMLInitializationEvent)` method is the central client initialization point. Do not construct a camera runtime before a world and player are available.
2. `src/main/java/com/alpharip/alphaclient/AlphaClientCommand.java`
   - Add a `recordingstudio` route that calls the client helper from AlphaClient.
   - Keep command handling on the Minecraft client thread.
3. `AlphaClient.registerClientCommands()` in AlphaClient.java
   - Add the `recordingstudio` command name to the existing command registration list.

### New integration code that must be created

1. **Minecraft framebuffer writer**
   - Port the existing `RecordingStudio/integration/MinecraftFramebufferFrameWriter.java` implementation from `mod-ideas` into `development`.
   - Preserve the client-thread OpenGL readback, restoration of the previously bound framebuffer and viewport, and asynchronous PNG file writing.
   - Keep the capture trigger on `RenderGameOverlayEvent.Post` with `ElementType.ALL`, before the active `GuiScreen` is drawn, so the Studio window is not recorded.
2. **Recording Studio bootstrap**
   - Create a small bootstrap/factory that constructs a `RecordingProject`, `RecordingCaptureSettings`, `RecordingCaptureService`, `Timeline`, and `RecordingStudioController`.
   - Choose and document the final local project directory during integration; the prototype currently does not establish a production storage path.
3. **Build Timelapse runtime construction**
   - Build the existing Build Timelapse service graph and pass it to `BuildTimelapseRuntime`.
   - Start one runtime after the world and player are available. Its `start()` method registers the Build Timelapse recording adapter with `RecordingStudioModuleRegistry`.
   - Shut it down on world unload. Avoid duplicate runtime instances and duplicate Forge event registrations.

### Runtime wiring sequence

1. On world load, create the Build Timelapse runtime and the Recording Studio services/runtime.
2. Start the Build Timelapse runtime once so its module adapter is registered.
3. When `/recordingstudio` is used, call `RecordingStudioRuntime.open(Minecraft.getMinecraft())`.
4. On world unload, shut down both runtimes and release camera/Forge event registrations.
5. Keep the Recording Studio session lifecycle explicit: closing its main screen currently shuts down the runtime and stops an active recording.

### Integration validation

- Confirm the selected Build Timelapse path is listed and can be prepared.
- Confirm the external camera state is reported by the module adapter.
- Confirm the HUD preview appears only for an active external camera, can be toggled without stopping capture, and supports dragging/resizing.
- Confirm normal gameplay recording does not show the external-camera preview.
- Build with the production ForgeGradle setup and perform the required in-game checks.

### Items that still require a source decision

- The exact world-load/world-unload event ownership and the Build Timelapse service graph must be matched to the current production lifecycle.
- The concrete framebuffer writer and final recording storage directory must be implemented/selected.
- The production command must be wired without replacing or overwriting the existing AlphaClient entrypoint.

## Launcher

Alpha Launcher is outside the scope of this branch. No launcher changes are required for the prototype.
