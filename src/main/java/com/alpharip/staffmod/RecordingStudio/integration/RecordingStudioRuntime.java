package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.camera.CameraController;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;
import com.alpharip.staffmod.RecordingStudio.module.RecordingStudioModule;
import com.alpharip.staffmod.RecordingStudio.module.RecordingStudioModuleRegistry;
import com.alpharip.staffmod.RecordingStudio.ui.RecordingStudioScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Independent Forge lifecycle for manual recording and the standalone camera. */
public final class RecordingStudioRuntime {
    private final RecordingStudioController controller;
    private CameraController camera;
    private CameraWorldOverlay overlay;
    private RecordingStudioCameraPreview cameraPreview;
    private boolean started;
    private boolean closed;
    private String selectedModuleId = "normal_gameplay";
    private String selectedOptionId;
    private String moduleStatus = "Using the current Minecraft camera.";

    public RecordingStudioRuntime(RecordingStudioController controller) {
        if (controller == null) throw new IllegalArgumentException("controller");
        this.controller = controller;
    }

    public synchronized void open(Minecraft minecraft) {
        if (minecraft == null || minecraft.theWorld == null || minecraft.thePlayer == null) {
            throw new IllegalStateException("Recording Studio can only open inside a loaded world");
        }
        if (closed) throw new IllegalStateException("Recording Studio runtime is shut down");
        if (!started) {
            camera = new CameraController(new MinecraftIndependentCameraBridge(minecraft));
            overlay = new CameraWorldOverlay(camera);
            cameraPreview = new RecordingStudioCameraPreview(this);
            started = true;
            try {
                MinecraftForge.EVENT_BUS.register(this);
                MinecraftForge.EVENT_BUS.register(overlay);
                MinecraftForge.EVENT_BUS.register(cameraPreview);
            } catch (RuntimeException registrationFailure) {
                shutdown();
                throw registrationFailure;
            }
        }
        minecraft.displayGuiScreen(new RecordingStudioScreen(controller, this));
    }

    public synchronized boolean isClosed() { return closed; }

    public synchronized CameraController getCameraController() {
        if (!started || camera == null) {
            throw new IllegalStateException("Recording Studio camera runtime is not started");
        }
        return camera;
    }

    public synchronized boolean hasExternalCamera() {
        if (camera != null && camera.isActive()) return true;
        RecordingStudioModule module = selectedModule();
        if (module == null) return false;
        try {
            return module.isExternalCameraActive();
        } catch (RuntimeException failure) {
            moduleStatus = "Could not read external camera state: " + failure.getMessage();
            return false;
        }
    }

    public synchronized void toggleCameraPreview() {
        if (cameraPreview != null) cameraPreview.toggle();
    }

    public synchronized boolean isCameraPreviewVisible() {
        return cameraPreview != null && cameraPreview.isVisible();
    }

    public synchronized void drawCameraPreview() {
        if (cameraPreview != null) cameraPreview.drawPreview();
    }

    public synchronized void drawLiveViewport(int x, int y, int width, int height) {
        if (cameraPreview != null) cameraPreview.drawLiveViewport(x, y, width, height);
    }

    public synchronized List<String> getModuleIds() {
        List<String> ids = new ArrayList<String>();
        ids.add("normal_gameplay");
        ids.add("build_timelapse");
        for (RecordingStudioModule module : RecordingStudioModuleRegistry.getAll()) {
            if (!ids.contains(module.getId())) ids.add(module.getId());
        }
        return Collections.unmodifiableList(ids);
    }

    public synchronized String getModuleDisplayName(String id) {
        if ("normal_gameplay".equals(id)) return "Normal Gameplay";
        if ("build_timelapse".equals(id)) return "Build Timelapse";
        RecordingStudioModule module = RecordingStudioModuleRegistry.get(id);
        if (module == null) return id;
        try {
            String name = module.getDisplayName();
            return name == null || name.trim().isEmpty() ? id : name;
        } catch (RuntimeException failure) {
            moduleStatus = "Could not read module name: " + failure.getMessage();
            return id;
        }
    }

    public synchronized String getSelectedModuleId() { return selectedModuleId; }

    public synchronized String getSelectedOptionId() {
        return selectedOptionId == null ? "" : selectedOptionId;
    }

    public synchronized List<String> getSelectedModuleOptions() {
        RecordingStudioModule module = selectedModule();
        if (module == null) return Collections.emptyList();
        try {
            List<String> options = module.getOptions();
            if (options == null || options.isEmpty()) return Collections.emptyList();
            return Collections.unmodifiableList(new ArrayList<String>(options));
        } catch (RuntimeException failure) {
            moduleStatus = "Could not read module options: " + failure.getMessage();
            return Collections.emptyList();
        }
    }

    public synchronized String getSelectedModuleStatus() {
        if ("normal_gameplay".equals(selectedModuleId)) return "Using the current Minecraft camera.";
        RecordingStudioModule module = selectedModule();
        if (module == null) {
            return "Module runtime is not initialized. Start the module runtime first.";
        }
        if (moduleStatus.startsWith("Module setup failed")
                || moduleStatus.startsWith("Module runtime is not initialized")
                || moduleStatus.startsWith("No saved camera paths")
                || moduleStatus.startsWith("No saved options")
                || moduleStatus.startsWith("Stop the recording")
                || moduleStatus.startsWith("The selected module")
                || moduleStatus.startsWith("Module cleanup failed")
                || moduleStatus.startsWith("Could not")) {
            return moduleStatus;
        }
        try {
            return module.getStatus();
        } catch (RuntimeException failure) {
            return "Module status unavailable: " + failure.getMessage();
        }
    }

    public synchronized void selectNextModule() {
        if (controller.getCapture().isRecording() || controller.getCapture().isPaused()) {
            moduleStatus = "Stop the recording before changing modules.";
            return;
        }
        List<String> ids = getModuleIds();
        int index = ids.indexOf(selectedModuleId);
        String next = ids.get((index + 1 + ids.size()) % ids.size());
        selectModule(next);
    }

    public synchronized void selectModule(String id) {
        if (controller.getCapture().isRecording() || controller.getCapture().isPaused()) {
            moduleStatus = "Stop the recording before changing modules.";
            return;
        }
        if (!stopSelectedModule()) return;

        String targetModuleId = id == null ? "normal_gameplay" : id;
        if (!"normal_gameplay".equals(targetModuleId) && camera != null && camera.isActive()) {
            try {
                camera.deactivate();
            } catch (Exception failure) {
                moduleStatus = "Could not release the standalone camera: " + failure.getMessage();
                return;
            }
        }
        selectedModuleId = targetModuleId;
        selectedOptionId = null;
        if ("normal_gameplay".equals(selectedModuleId)) {
            moduleStatus = "Using the current Minecraft camera.";
            return;
        }

        RecordingStudioModule module = RecordingStudioModuleRegistry.get(selectedModuleId);
        if (module == null) {
            moduleStatus = "Module runtime is not initialized. Open the module once, then return to Recording Studio.";
            return;
        }
        try {
            if (!module.isAvailable()) {
                moduleStatus = "The selected module " + module.getDisplayName() + " is currently unavailable.";
                return;
            }

            List<String> options = module.getOptions();
            if (options == null || options.isEmpty()) {
                moduleStatus = "No saved camera paths. Create a path in the Build Timelapse Camera Editor first.";
                return;
            }
            selectOption(options.get(0));
        } catch (RuntimeException failure) {
            moduleStatus = "Could not prepare module: " + failure.getMessage();
        }
    }

    public synchronized void selectNextModuleOption() {
        if (controller.getCapture().isRecording() || controller.getCapture().isPaused()) {
            moduleStatus = "Stop the recording before changing the module option.";
            return;
        }
        RecordingStudioModule module = selectedModule();
        if (module == null) {
            moduleStatus = "The selected module is not initialized.";
            return;
        }
        try {
            List<String> options = module.getOptions();
            if (options == null || options.isEmpty()) {
                moduleStatus = "No saved options are available for this module.";
                return;
            }
            int index = options.indexOf(selectedOptionId);
            selectOption(options.get((index + 1 + options.size()) % options.size()));
        } catch (RuntimeException failure) {
            moduleStatus = "Could not read module options: " + failure.getMessage();
        }
    }

    private void selectOption(String optionId) {
        RecordingStudioModule module = selectedModule();
        if (module == null) return;
        try {
            module.onRecordingStop();
            module.prepare(optionId);
            selectedOptionId = optionId;
            moduleStatus = module.getStatus();
        } catch (Exception failure) {
            selectedOptionId = null;
            try { module.onRecordingStop(); }
            catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            moduleStatus = "Module setup failed: " + failure.getMessage();
        }
    }

    public synchronized void startRecording() {
        RecordingStudioModule module = selectedModule();
        if (!"normal_gameplay".equals(selectedModuleId) && module == null) {
            moduleStatus = "Module runtime is not initialized. Recording was not started.";
            return;
        }
        if (module != null) {
            try {
                if (!module.isAvailable()) {
                    moduleStatus = "The selected module " + module.getDisplayName() + " is unavailable. Recording was not started.";
                    return;
                }
            } catch (RuntimeException failure) {
                moduleStatus = "Could not check module availability: " + failure.getMessage();
                return;
            }
        }
        try {
            if (module != null) module.onRecordingStart(System.currentTimeMillis());
            controller.start();
            if (!controller.getCapture().isRecording() && module != null) module.onRecordingStop();
        } catch (Exception failure) {
            if (module != null) {
                try { module.onRecordingStop(); }
                catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            }
            moduleStatus = "Could not start module recording: " + failure.getMessage();
        }
    }

    public synchronized void pauseRecording() {
        if (!controller.getCapture().isRecording()) return;
        RecordingStudioModule module = selectedModule();
        try {
            if (module != null) module.onRecordingPause(System.currentTimeMillis());
        } catch (Exception failure) {
            moduleStatus = "Module pause failed: " + failure.getMessage();
            return;
        }
        controller.pause();
    }

    public synchronized void resumeRecording() {
        if (!controller.getCapture().isPaused()) return;
        RecordingStudioModule module = selectedModule();
        boolean moduleResumed = false;
        try {
            if (module != null) {
                module.onRecordingResume(System.currentTimeMillis());
                moduleResumed = true;
            }
            controller.resume();
            if (!controller.getCapture().isRecording() && module != null) {
                module.onRecordingPause(System.currentTimeMillis());
            }
        } catch (Exception failure) {
            if (moduleResumed && module != null) {
                try { module.onRecordingPause(System.currentTimeMillis()); }
                catch (Exception rollbackFailure) { failure.addSuppressed(rollbackFailure); }
            }
            moduleStatus = "Module resume failed: " + failure.getMessage();
        }
    }

    public synchronized void stopRecording() {
        controller.stop();
        RecordingStudioModule module = selectedModule();
        if (module != null) {
            try { module.onRecordingStop(); }
            catch (Exception failure) { moduleStatus = "Module stop failed: " + failure.getMessage(); }
        }
    }

    private RecordingStudioModule selectedModule() {
        if ("normal_gameplay".equals(selectedModuleId)) return null;
        return RecordingStudioModuleRegistry.get(selectedModuleId);
    }

    private boolean stopSelectedModule() {
        RecordingStudioModule module = selectedModule();
        if (module == null) return true;
        try {
            module.onRecordingStop();
            return true;
        } catch (Exception failure) {
            moduleStatus = "Module cleanup failed: " + failure.getMessage();
            return false;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.theWorld == null) {
            shutdown();
            return;
        }
        controller.tick(System.currentTimeMillis());
        if ("normal_gameplay".equals(selectedModuleId) && camera != null) {
            try { camera.tick(System.currentTimeMillis()); }
            catch (Exception failure) {
                try { camera.deactivate(); }
                catch (Exception ignored) { }
                moduleStatus = "Studio camera stopped: " + failure.getMessage();
            }
        }
    }

    /** Applies the saved camera roll only while the Studio-owned render-view camera is active. */
    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        CameraController activeCamera;
        synchronized (this) {
            activeCamera = camera;
        }
        if (activeCamera == null || !activeCamera.isActive() || minecraft == null
                || event.entity != minecraft.getRenderViewEntity()) return;
        event.setRoll(activeCamera.getPose().roll);
    }

    private boolean usesDesktopCapture() {
        com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioScene scene = controller.getActiveScene();
        if (scene == null) return false;
        com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource desktop =
                scene.findSource(com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource.Type.DESKTOP);
        com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource display =
                scene.findSource(com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource.Type.DISPLAY);
        return (desktop != null && desktop.isEnabled()) || (display != null && display.isEnabled());
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL && !usesDesktopCapture()) {
            controller.captureFrameTick(System.currentTimeMillis());
        }
    }

    /** Desktop screenshots are taken after the full Minecraft frame, including its open GUI, is rendered. */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && usesDesktopCapture()) {
            controller.captureFrameTick(System.currentTimeMillis());
        }
    }

    /** Fallback for the Minecraft hide-GUI setting, which suppresses overlay events. */
    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!usesDesktopCapture() && minecraft != null && minecraft.gameSettings != null
                && minecraft.gameSettings.hideGUI) {
            controller.captureFrameTick(System.currentTimeMillis());
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote) shutdown();
    }

    public synchronized void shutdown() {
        if (closed) return;
        closed = true;
        stopSelectedModule();
        if (started) {
            MinecraftForge.EVENT_BUS.unregister(this);
            MinecraftForge.EVENT_BUS.unregister(overlay);
            MinecraftForge.EVENT_BUS.unregister(cameraPreview);
        }
        if (cameraPreview != null) cameraPreview.dispose();
        if (camera != null) {
            try { camera.deactivate(); }
            catch (Exception ignored) { }
        }
        started = false;
        controller.shutdown();
    }
}
