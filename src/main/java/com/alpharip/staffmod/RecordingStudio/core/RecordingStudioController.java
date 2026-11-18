package com.alpharip.staffmod.RecordingStudio.core;

import com.alpharip.staffmod.RecordingStudio.audio.RecordingAudioCaptureService;
import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportService;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import java.io.File;
import java.io.IOException;

/** Owns a general-purpose recording session independently of any feature module. */
public final class RecordingStudioController {
    private final RecordingProject project;
    private final RecordingCaptureService capture;
    private final Timeline timeline;
    private final RecordingStudioProjectStore projectStore;
    private final RecordingAudioCaptureService audioCapture;
    private final File audioDirectory;
    private final RecordingStudioExportService exportService;
    private boolean closed;
    private boolean sessionStartedInThisRuntime;
    private long lastRecoveryCheckpointAt;
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
        this.capture.setIntervalMillis(project.getCaptureIntervalMillis());
        this.timeline = timeline;
        this.projectStore = new RecordingStudioProjectStore(projectDirectory);
        File recordingRoot = projectDirectory.getParentFile() == null ? projectDirectory : projectDirectory.getParentFile();
        this.audioDirectory = new File(new File(recordingRoot, "recordings"), project.getId() + "/audio");
        File framesDirectory = new File(new File(recordingRoot, "recordings"), project.getId() + "/frames");
        File exportDirectory = new File(new File(recordingRoot, "exports"), project.getId());
        this.exportService = new RecordingStudioExportService(framesDirectory, exportDirectory, timeline);
        this.audioCapture = new RecordingAudioCaptureService(new RecordingAudioCaptureService.Listener() {
            public void onAudioSaved(File file, long startMillis, long durationMillis) {
                synchronized (RecordingStudioController.this) {
                    try {
                        TimelineTrack audioTrack = null;
                        for (TimelineTrack track : timeline.getTracks()) {
                            if ("audio".equals(track.getId())) { audioTrack = track; break; }
                        }
                        if (audioTrack == null) {
                            audioTrack = new TimelineTrack("audio", "Audio");
                            timeline.addTrack(audioTrack);
                        }
                        if (durationMillis > 0L) {
                            audioTrack.add(new TimelineClip("audio-" + java.util.UUID.randomUUID().toString(),
                                    file.getAbsolutePath(), startMillis, durationMillis));
                        }
                        if (project.isRecording()) {
                            long checkpointAt = System.currentTimeMillis();
                            project.checkpoint(checkpointAt);
                            lastRecoveryCheckpointAt = checkpointAt;
                        }
                        projectStore.save(project, timeline);
                        finishRecoveryMarkerIfIdle();
                        status = "Audio saved to timeline";
                    } catch (IOException | RuntimeException failure) {
                        status = "Audio saved; timeline update failed: " + failure.getMessage();
                    }
                }
            }
            public void onAudioFailed(String message) {
                synchronized (RecordingStudioController.this) {
                    status = "Audio capture failed: " + message;
                    try { finishRecoveryMarkerIfIdle(); }
                    catch (IOException failure) { status += " — Recovery marker retained: " + failure.getMessage(); }
                }
            }
        });
        try {
            boolean recoveryMarker = this.projectStore.hasRecoveryMarker(project.getId());
            if (recoveryMarker || project.isRecording()) {
                project.recoverInterruptedSession(System.currentTimeMillis());
                this.projectStore.save(project, timeline);
                if (recoveryMarker) this.projectStore.clearRecordingActive(project.getId());
                status = "Interrupted session recovered; existing frames are available";
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
            lastRecoveryCheckpointAt = now;
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
        audioCapture.stop();
        long now = System.currentTimeMillis();
        capture.pause();
        project.pause(now);
        try {
            projectStore.save(project, timeline);
            projectStore.clearRecordingActive(project.getId());
            sessionStartedInThisRuntime = false;
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
            sessionStartedInThisRuntime = true;
            lastRecoveryCheckpointAt = now;
            status = "Recording";
        } catch (IOException failure) {
            capture.pause();
            project.pause(System.currentTimeMillis());
            status = "Resume checkpoint failed: " + failure.getMessage();
        }
    }

    public synchronized void stop() {
        if (closed || (!capture.isRecording() && !capture.isPaused())) return;
        audioCapture.stop();
        long now = System.currentTimeMillis();
        capture.stop();
        project.stop(now);
        try {
            projectStore.save(project, timeline);
            if (capture.getPendingFrameCount() == 0 && !audioCapture.isFinalizing()) {
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

    public synchronized void startAudioCapture() {
        ensureOpen();
        if (!capture.isRecording()) {
            status = "Start video recording before capturing audio";
            return;
        }
        try {
            long timelineStart = project.getElapsedMillis(System.currentTimeMillis());
            audioCapture.start(audioDirectory, "audio-" + java.util.UUID.randomUUID().toString() + ".wav",
                    timelineStart);
            status = "Microphone recording";
        } catch (Exception failure) {
            status = "Cannot start microphone: " + failure.getMessage();
        }
    }

    public synchronized void stopAudioCapture() {
        audioCapture.stop();
        if (audioCapture.isFinalizing()) status = "Finalizing microphone audio";
    }

    public synchronized String getAudioStatus() {
        if (audioCapture.isRecording()) return "Microphone: Recording";
        if (audioCapture.isFinalizing()) return "Microphone: Finalizing";
        return "Microphone: Idle";
    }

    public synchronized void startExport(RecordingStudioExportService.Format format) {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before exporting";
            return;
        }
        if (capture.getPendingFrameCount() > 0 || audioCapture.isFinalizing()) {
            status = "Wait for pending frame and audio writes before exporting";
            return;
        }
        try {
            int exportFps = Math.max(1, Math.min(240,
                    (int) Math.round(1000.0 / capture.getIntervalMillis())));
            exportService.start(format, exportFps, "ffmpeg");
            status = format.name() + " export started";
        } catch (IOException | RuntimeException failure) {
            status = "Cannot start export: " + failure.getMessage();
        }
    }

    public synchronized void cancelExport() {
        exportService.cancel();
        status = "Export cancellation requested";
    }

    public synchronized String getExportStatus() { return exportService.getStatus(); }

    public synchronized void adjustCaptureInterval(long deltaMillis) {
        ensureOpen();
        long adjusted = Math.max(100L, Math.min(60000L, capture.getIntervalMillis() + deltaMillis));
        capture.setIntervalMillis(adjusted);
        project.setCaptureIntervalMillis(adjusted);
        if (project.isRecording()) project.checkpoint(System.currentTimeMillis());
        try {
            projectStore.save(project, timeline);
            status = "Capture interval saved: " + adjusted + " ms";
        } catch (IOException failure) {
            status = "Capture interval changed but could not be saved: " + failure.getMessage();
        }
    }

    public synchronized void tick(long now) {
        if (closed) return;
        capture.tick(now);
        if (capture.isRecording() && now - lastRecoveryCheckpointAt >= 1000L) {
            try {
                project.checkpoint(now);
                projectStore.saveProject(project);
                projectStore.markRecordingActive(project);
                lastRecoveryCheckpointAt = now;
            } catch (IOException failure) {
                status = "Recovery checkpoint failed: " + failure.getMessage();
            }
        }
        if (sessionStartedInThisRuntime && !capture.isRecording() && !capture.isPaused()
                && capture.getPendingFrameCount() == 0 && !audioCapture.isFinalizing()) {
            try {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
                status = "Stopped — project checkpoint saved";
            } catch (IOException failure) {
                status = "Stopped; recovery marker retained: " + failure.getMessage();
            }
        }
    }

    private void finishRecoveryMarkerIfIdle() throws IOException {
        if (sessionStartedInThisRuntime && !capture.isRecording() && !capture.isPaused()
                && capture.getPendingFrameCount() == 0 && !audioCapture.isFinalizing()) {
            projectStore.clearRecordingActive(project.getId());
            sessionStartedInThisRuntime = false;
        }
    }

    public synchronized void save() throws IOException {
        ensureOpen();
        if (project.isRecording()) project.checkpoint(System.currentTimeMillis());
        projectStore.save(project, timeline);
        status = "Project and timeline saved";
    }

    public Timeline getTimeline() { return timeline; }
    public RecordingProject getProject() { return project; }
    public RecordingCaptureService getCapture() { return capture; }
    public RecordingAudioCaptureService getAudioCapture() { return audioCapture; }

    public synchronized String getStatus() {
        String error = capture.getLastError();
        return error == null ? status : status + " — Capture error: " + error;
    }

    public synchronized void shutdown() {
        if (closed) return;
        stop();
        try {
            projectStore.save(project, timeline);
            if (sessionStartedInThisRuntime && capture.getPendingFrameCount() == 0
                    && !audioCapture.isFinalizing()) {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
            }
            status = "Project and timeline saved";
        } catch (IOException failure) {
            status = "Shutdown save failed; recovery marker retained: " + failure.getMessage();
        }
        capture.shutdown();
        if (sessionStartedInThisRuntime || audioCapture.isFinalizing()) {
            final RecordingCaptureService pendingCapture = capture;
            final RecordingStudioProjectStore pendingStore = projectStore;
            final String projectId = project.getId();
            final RecordingAudioCaptureService audioCapture = this.audioCapture;
            Thread finalizer = new Thread(new Runnable() {
                public void run() {
                    try {
                        audioCapture.awaitCompletion();
                        while (!pendingCapture.awaitPendingWrites(1L, java.util.concurrent.TimeUnit.MINUTES)) {
                            // Keep the recovery marker until all accepted frame writes have completed.
                        }
                        synchronized (RecordingStudioController.this) {
                            projectStore.save(project, timeline);
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
