package com.alpharip.staffmod.RecordingStudio.core;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;
import java.io.File;
import java.io.IOException;

/** Owns a general-purpose recording session independently of any feature module. */
public final class RecordingStudioController {
    private final RecordingProject project;
    private final RecordingCaptureService capture;
    private final File projectDirectory;
    private final Timeline timeline;\n    private final RecordingStudioProjectStore projectStore;
    private boolean closed;
    private String status = "Ready";

    public RecordingStudioController(RecordingProject project, RecordingCaptureService capture, File projectDirectory) {
        if (project == null || capture == null || projectDirectory == null) {
            throw new IllegalArgumentException("Project, capture service and project directory are required");
        }
        this.project = project;
        this.capture = capture;
        this.projectDirectory = projectDirectory;
        timeline.addTrack(new TimelineTrack("video", "Video"));
        timeline.addTrack(new TimelineTrack("audio", "Audio"));
    }

    public RecordingStudioController(RecordingProject project, RecordingCaptureService capture, File projectDirectory, Timeline timeline) {\n        if (project == null || capture == null || projectDirectory == null || timeline == null) {\n            throw new IllegalArgumentException("Project, capture service, project directory and timeline are required");\n        }\n        this.project = project;\n        this.capture = capture;\n        this.projectDirectory = projectDirectory;\n        this.timeline = timeline;\n        this.projectStore = new RecordingStudioProjectStore(projectDirectory);\n    }\n\n    private static Timeline defaultTimeline() {\n        Timeline value = new Timeline();\n        value.addTrack(new TimelineTrack("video", "Video"));\n        value.addTrack(new TimelineTrack("audio", "Audio"));\n        return value;\n    }\n\n    public synchronized void start() {
        ensureOpen();
        if (capture.isRecording()) {
            status = "Recording";
            return;
        }
        if (capture.isPaused()) {
            resume();
            return;
        }
        try {
            long now = System.currentTimeMillis();
            capture.start(now);
            project.start(now);
            status = "Recording";
        } catch (IOException failure) {
            status = "Cannot start: " + failure.getMessage();
        }
    }

    public synchronized void pause() {
        ensureOpen();
        if (!capture.isRecording()) {
            return;
        }
        long now = System.currentTimeMillis();
        capture.pause();
        project.pause(now);
        status = "Paused";
    }

    public synchronized void resume() {
        ensureOpen();
        if (!capture.isPaused()) {
            return;
        }
        long now = System.currentTimeMillis();
        capture.resume(now);
        project.resume(now);
        status = "Recording";
    }

    public synchronized void stop() {
        if (closed) {
            return;
        }
        if (!capture.isRecording() && !capture.isPaused()) {
            return;
        }
        long now = System.currentTimeMillis();
        capture.stop();
        project.stop(now);
        status = "Stopped";
    }

    public synchronized void adjustCaptureInterval(long deltaMillis) {
        ensureOpen();
        long current = capture.getIntervalMillis();
        long adjusted = Math.max(100L, Math.min(60000L, current + deltaMillis));
        capture.setIntervalMillis(adjusted);
        status = "Capture interval: " + adjusted + " ms";
    }

    public synchronized void tick(long now) {
        if (!closed) {
            capture.tick(now);
        }
    }

    public synchronized void save() throws IOException {
        ensureOpen();
        project.save(projectDirectory);
        status = "Project saved";
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public RecordingProject getProject() {
        return project;
    }

    public RecordingCaptureService getCapture() {
        return capture;
    }

    public synchronized String getStatus() {
        String error = capture.getLastError();
        if (error == null) {
            return status;
        }
        return status + " — Capture error: " + error;
    }

    public synchronized void shutdown() {
        if (closed) {
            return;
        }
        stop();
        capture.shutdown();
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Recording Studio is shut down");
        }
    }
}
