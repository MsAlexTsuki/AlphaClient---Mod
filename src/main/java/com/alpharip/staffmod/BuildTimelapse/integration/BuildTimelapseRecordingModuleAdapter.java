package com.alpharip.staffmod.BuildTimelapse.integration;

import com.alpharip.staffmod.BuildTimelapse.core.BuildTimelapseController;
import com.alpharip.staffmod.RecordingStudio.camera.CameraController;
import com.alpharip.staffmod.RecordingStudio.camera.CameraMode;
import com.alpharip.staffmod.RecordingStudio.camera.CameraPath;
import com.alpharip.staffmod.RecordingStudio.camera.CameraWaypoint;
import com.alpharip.staffmod.RecordingStudio.camera.FileCameraPathStore;
import com.alpharip.staffmod.RecordingStudio.module.RecordingStudioModule;

import java.util.Collections;
import java.util.List;

/**
 * Exposes Build Timelapse camera paths to Recording Studio without making
 * Recording Studio depend on Build Timelapse classes.
 */
public final class BuildTimelapseRecordingModuleAdapter implements RecordingStudioModule {
    private final BuildTimelapseController controller;
    private final FileCameraPathStore pathStore;
    private CameraPath selectedPath;
    private String selectedPathId;
    private boolean prepared;

    public BuildTimelapseRecordingModuleAdapter(BuildTimelapseController controller,
                                                FileCameraPathStore pathStore) {
        if (controller == null || pathStore == null) throw new IllegalArgumentException();
        this.controller = controller;
        this.pathStore = pathStore;
    }

    @Override public String getId() { return "build_timelapse"; }
    @Override public String getDisplayName() { return "Build Timelapse"; }
    @Override public boolean isAvailable() { return true; }

    @Override
    public List<String> getOptions() {
        List<String> ids = pathStore.listIds();
        return ids == null ? Collections.<String>emptyList() : ids;
    }

    @Override
    public synchronized void prepare(String optionId) throws Exception {
        if (optionId == null || optionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Select a saved Build Timelapse camera path");
        }
        if (controller.isActive()) {
            throw new IllegalStateException("Stop the separate Build Timelapse recording before using its camera here");
        }
        CameraController camera = controller.getCamera();
        if (camera.isActive()) {
            throw new IllegalStateException("The Build Timelapse camera is already in use");
        }

        CameraPath loaded = pathStore.load(optionId);
        List<CameraWaypoint> points = loaded.getWaypoints();
        if (points.size() < 2 || loaded.duration() <= 0L) {
            throw new IllegalStateException("The selected camera path needs at least two timed waypoints");
        }

        try {
            camera.activate();
            camera.setPose(points.get(0).getPose());
            selectedPath = loaded;
            selectedPathId = optionId;
            prepared = true;
        } catch (Exception failure) {
            try { camera.deactivate(); }
            catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            throw failure;
        }
    }

    @Override
    public synchronized void onRecordingStart(long now) throws Exception {
        if (!prepared) prepare(selectedPathId);
        controller.getCamera().playPath(selectedPath, now);
    }

    @Override
    public synchronized void onRecordingPause(long now) {
        if (prepared) controller.getCamera().pausePath(now);
    }

    @Override
    public synchronized void onRecordingResume(long now) {
        if (prepared) controller.getCamera().resumePath(now);
    }

    @Override
    public synchronized void onRecordingStop() throws Exception {
        if (controller.isActive()) {
            throw new IllegalStateException("The separate Build Timelapse recording is using this camera");
        }
        CameraController camera = controller.getCamera();
        camera.stopPath();
        try {
            camera.deactivate();
        } finally {
            selectedPath = null;
            prepared = false;
        }
    }

    @Override
    public synchronized String getStatus() {
        if (!prepared) return "No Build Timelapse camera path prepared";
        if (controller.getCamera().getMode() == CameraMode.PATH) return "Build Timelapse camera path is running";
        return "Build Timelapse camera positioned at the selected path";
    }
}
