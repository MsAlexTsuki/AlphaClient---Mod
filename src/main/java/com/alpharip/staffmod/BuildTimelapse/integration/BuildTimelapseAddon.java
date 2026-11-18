package com.alpharip.staffmod.BuildTimelapse.integration;

import com.alpharip.staffmod.BuildTimelapse.BuildTimelapse;
import com.alpharip.staffmod.BuildTimelapse.analytics.BuildActivityTracker;
import com.alpharip.staffmod.BuildTimelapse.capture.CaptureManifest;
import com.alpharip.staffmod.BuildTimelapse.capture.CaptureService;
import com.alpharip.staffmod.BuildTimelapse.capture.CaptureSettings;
import com.alpharip.staffmod.BuildTimelapse.capture.MinecraftFramebufferScreenshotBridge;
import com.alpharip.staffmod.BuildTimelapse.capture.MinecraftRenderThreadExecutor;
import com.alpharip.staffmod.BuildTimelapse.capture.MinecraftScreenshotCaptureBackend;
import com.alpharip.staffmod.BuildTimelapse.core.BuildTimelapseProject;
import com.alpharip.staffmod.BuildTimelapse.core.FileProjectStore;
import com.alpharip.staffmod.BuildTimelapse.effects.EffectsController;
import com.alpharip.staffmod.BuildTimelapse.effects.FileEffectsProfileStore;
import com.alpharip.staffmod.BuildTimelapse.effects.VisualEffectsProfile;
import com.alpharip.staffmod.BuildTimelapse.export.ExportQueue;
import com.alpharip.staffmod.BuildTimelapse.ui.StudioController;
import com.alpharip.staffmod.BuildTimelapse.ui.StudioScreenModel;
import com.alpharip.staffmod.RecordingStudio.camera.CameraController;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/** Creates the optional Build Timelapse services inside the Alpha Recording Studio addon. */
public final class BuildTimelapseAddon {
    private static final String DEFAULT_PROJECT_ID = "default";
    private static BuildTimelapseRuntime runtime;
    private static StudioController screenController;
    private static StudioScreenModel screenModel;

    private BuildTimelapseAddon() { }

    public static synchronized void initialize(Minecraft minecraft) throws IOException {
        if (minecraft == null || minecraft.mcDataDir == null) {
            throw new IllegalStateException("Minecraft client storage is unavailable");
        }
        if (runtime != null && runtime.isStarted()) return;

        File root = new File(minecraft.mcDataDir, "buildtimelapse");
        File projectDirectory = new File(root, "projects");
        FileProjectStore projectStore = new FileProjectStore(projectDirectory);
        File projectFile = new File(projectDirectory, DEFAULT_PROJECT_ID + ".btproj");

        BuildTimelapseProject project;
        Timeline timeline;
        if (projectFile.isFile()) {
            project = projectStore.load(DEFAULT_PROJECT_ID);
            timeline = projectStore.loadTimeline(DEFAULT_PROJECT_ID);
        } else {
            String worldName = minecraft.theWorld == null
                    ? "Minecraft World" : minecraft.theWorld.getWorldInfo().getWorldName();
            project = new BuildTimelapseProject(DEFAULT_PROJECT_ID, "Build Timelapse", worldName,
                    System.currentTimeMillis());
            timeline = new Timeline();
        }
        ensureTrack(timeline, "video", "Video");
        ensureTrack(timeline, "audio", "Audio");

        File sessionDirectory = new File(new File(root, "recordings"), DEFAULT_PROJECT_ID);
        File framesDirectory = new File(sessionDirectory, "frames");
        CaptureSettings captureSettings = new CaptureSettings();
        CaptureService capture = new CaptureService(
                new MinecraftScreenshotCaptureBackend(
                        new MinecraftFramebufferScreenshotBridge(minecraft),
                        new MinecraftRenderThreadExecutor(minecraft)),
                captureSettings, framesDirectory);

        CameraController camera = new CameraController(new MinecraftIndependentCameraBridge(minecraft));
        BuildActivityTracker activity = new BuildActivityTracker();
        VisualEffectsProfile effectsProfile = loadEffectsProfile(root);
        EffectsController effects = new EffectsController(
                new MinecraftEffectsRuntimePort(minecraft), effectsProfile);
        ExportQueue exports = new ExportQueue();
        CaptureManifest manifest = BuildTimelapse.createLocalCaptureManifest(
                new File(sessionDirectory, "capture-manifest.csv"));

        BuildTimelapse studio = new BuildTimelapse(project, camera, capture, timeline, activity,
                effects, exports, projectStore, manifest);
        BuildTimelapseRuntime nextRuntime = new BuildTimelapseRuntime(studio);
        try {
            nextRuntime.start();
        } catch (RuntimeException failure) {
            studio.getController().shutdown();
            throw failure;
        }

        StudioScreenModel nextModel = new StudioScreenModel();
        StudioController nextController = new StudioController(studio.getController(), nextModel);
        runtime = nextRuntime;
        screenModel = nextModel;
        screenController = nextController;
    }

    public static synchronized void openStudio(Minecraft minecraft) throws IOException {
        if (minecraft == null || minecraft.theWorld == null || minecraft.thePlayer == null) {
            throw new IllegalStateException("Build Timelapse Studio can only open inside a loaded world");
        }
        initialize(minecraft);
        runtime.openStudio(minecraft, screenController, screenModel);
    }

    public static synchronized boolean isInitialized() {
        return runtime != null && runtime.isStarted();
    }

    private static VisualEffectsProfile loadEffectsProfile(File root) throws IOException {
        FileEffectsProfileStore store = new FileEffectsProfileStore(new File(root, "effects"));
        try {
            return store.load(DEFAULT_PROJECT_ID);
        } catch (FileNotFoundException missing) {
            return new VisualEffectsProfile();
        }
    }

    private static void ensureTrack(Timeline timeline, String id, String name) {
        for (TimelineTrack track : timeline.getTracks()) {
            if (id.equals(track.getId())) return;
        }
        timeline.addTrack(new TimelineTrack(id, name));
    }
}
