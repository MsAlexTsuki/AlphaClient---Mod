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
    private final Timeline timeline;
    private final RecordingStudioProjectStore projectStore;
    private boolean closed;
    private boolean sessionStartedInThisRuntime;
    private String status = "Ready";

    public RecordingStudioController(RecordingProject project, RecordingCaptureService capture, File projectDirectory) {
        this(project, capture, projectDirectory, defaultTimeline());
    }

    public RecordingStudioController(RecordingProject project, RecordingCaptureService capture,
                                     File projectDirectory, Timeline timeline) {
        if (project == null || capture == null || projectDirectory == null || timeline == null) {
            throw new IllegalArgumentException("Project, capture service, project directory and timeline are required");
        }
        this.project = project;
        this.capture = capture;
        this.timeline = timeline;
        this.projectStore = new RecordingStudioProjectStore(projectDirectory);
        try {
            if (this.projectStore.hasRecoveryMarker(project.getId())) {
                status = "Interrupted session detected; existing frames are available";
            }
        } catch (IOException failure) {
            status = "Recovery check failed: " + failure.getMessage();
        }
    }

    private static Timeline defaultTimeline() {
        Timeline value = new Timeline();
        value.addTrack(new TimelineTrack("video", "Video"));
        value.addTrack(new TimelineTrack("audio", "Audio"));
        return value;
    }

    public synchronized void start() {
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
            projectStore.save(project, timeline);
            projectStore.markRecordingActive(project);
            sessionStartedInThisRuntime = true;
            status = "Recording";
        } catch (IOException failure) {
            capture.stop();
            project.stop(System.currentTimeMillis());
            status = "Cannot start: " + failure.getMessage();
        }
    }

    public synchronized void pause() {
        ensureOpen();
        if (!capture.isRecording()) return;
        long now = System.currentTimeMillis();
        capture.pause();
        project.pause(now);
        try {
            projectStore.save(project, timeline);
            status = "Paused — recovery checkpoint saved";
        } catch (IOException failure) {
            status = "Pause checkpoint failed: " + failure.getMessage();
        }
    }

    public synchronized void resume() {
        ensureOpen();
        if (!capture.isPaused()) return;
        long now = System.currentTimeMillis();
        capture.resume(now);
        project.resume(now);
        try {
            projectStore.save(project, timeline);
            projectStore.markRecordingActive(project);
            status = "Recording";
        } catch (IOException failure) {
            capture.pause();
            project.pause(System.currentTimeMillis());
            status = "Resume checkpoint failed: " + failure.getMessage();
        }
    }

    public synchronized void stop() {
        if (closed || (!capture.isRecording() && !capture.isPaused())) return;
        long now = System.currentTimeMillis();
        capture.stop();
        project.stop(now);
        try {
            projectStore.save(project, timeline);
            if (capture.getPendingFrameCount() == 0) {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
                status = "Stopped — project checkpoint saved";
            } else {
                status = "Stopped — waiting for pending frame writes";
            }
        } catch (IOException failure) {
            status = "Stopped; recovery marker retained: " + failure.getMessage();
        }
    }

    public synchronized void adjustCaptureInterval(long deltaMillis) {
        ensureOpen();
        long adjusted = Math.max(100L, Math.min(60000L, capture.getIntervalMillis() + deltaMillis));
        capture.setIntervalMillis(adjusted);
        status = "Capture interval: " + adjusted + " ms";
    }

    public synchronized void tick(long now) {
        if (closed) return;
        capture.tick(now);
        if (sessionStartedInThisRuntime && !capture.isRecording() && !capture.isPaused()
                && capture.getPendingFrameCount() == 0) {
            try {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
                status = "Stopped — project checkpoint saved";
            } catch (IOException failure) {
                status = "Stopped; recovery marker retained: " + failure.getMessage();
            }
        }
    }

    public synchronized void save() throws IOException {
        ensureOpen();
        projectStore.save(project, timeline);
        status = "Project and timeline saved";
    }

    public Timeline getTimeline() { return timeline; }
    public RecordingProject getProject() { return project; }
    public RecordingCaptureService getCapture() { return capture; }

    public synchronized String getStatus() {
        String error = capture.getLastError();
        return error == null ? status : status + " — Capture error: " + error;
    }

    public synchronized void shutdown() {
        if (closed) return;
        stop();
        try {
            projectStore.save(project, timeline);
            if (sessionStartedInThisRuntime && capture.getPendingFrameCount() == 0) {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
            }
            status = "Project and timeline saved";
        } catch (IOException failure) {
            status = "Shutdown save failed; recovery marker retained: " + failure.getMessage();
        }
        capture.shutdown();
        if (sessionStartedInThisRuntime) {
            final RecordingCaptureService pendingCapture = capture;
            final RecordingStudioProjectStore pendingStore = projectStore;
            final String projectId = project.getId();
            Thread finalizer = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (!pendingCapture.awaitPendingWrites(1L, java.util.concurrent.TimeUnit.MINUTES)) {
                            // Keep the recovery marker until all accepted frame writes have completed.
                        }
                        pendingStore.clearRecordingActive(projectId);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    } catch (IOException ignored) {
                        // Retain the marker when final cleanup cannot be confirmed.
                    }
                }
            }, "Alpha-RecordingStudio-RecoveryFinalizer");
            finalizer.setDaemon(true);
            finalizer.start();
        }
        closed = true;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Recording Studio is shut down");
    }
}
