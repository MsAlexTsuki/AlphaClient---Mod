package com.alpharip.staffmod.RecordingStudio.core;

import com.alpharip.staffmod.RecordingStudio.audio.RecordingAudioCaptureService;
import com.alpharip.staffmod.RecordingStudio.integration.DesktopScreenFrameWriter;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioExportHud;
import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportService;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioScene;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource;
import java.io.File;
import java.io.IOException;

/** Owns a general-purpose recording session independently of any feature module. */
public final class RecordingStudioController {
    private static final String SYSTEM_DEFAULT_MICROPHONE = "__ALPHA_SYSTEM_DEFAULT__";
    private final RecordingProject project;
    private final RecordingCaptureService capture;
    private final Timeline timeline;
    private final RecordingStudioProjectStore projectStore;
    private final RecordingStudioStorageUsage storageUsage;
    private final RecordingAudioCaptureService audioCapture;
    private final RecordingAudioCaptureService gameAudioCapture;
    private final File audioDirectory;
    private final File gameAudioDirectory;
    private final File framesDirectory;
    private final RecordingStudioExportService exportService;
    private final com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportSettings exportSettings =
            new com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportSettings();
    private boolean closed;
    private boolean sessionStartedInThisRuntime;
    private boolean resumeMicrophoneAfterPause;
    private boolean resumeGameAudioAfterPause;
    private long lastRecoveryCheckpointAt;
    private String microphoneSelectionName = "System Default";
    private String gameAudioSelectionName = "No loopback input selected";
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
        this.microphoneSelectionName = resolveMicrophoneName(project.getMicrophoneDeviceKey());
        this.capture = capture;
        this.capture.setIntervalMillis(project.getCaptureIntervalMillis());
        this.timeline = timeline;
        this.projectStore = new RecordingStudioProjectStore(projectDirectory);
        File recordingRoot = projectDirectory.getParentFile() == null ? projectDirectory : projectDirectory.getParentFile();
        this.storageUsage = new RecordingStudioStorageUsage(recordingRoot);
        this.audioDirectory = new File(new File(recordingRoot, "recordings"), project.getId() + "/audio");
        this.gameAudioDirectory = new File(new File(recordingRoot, "recordings"), project.getId() + "/game-audio");
        this.framesDirectory = new File(new File(recordingRoot, "recordings"), project.getId() + "/frames");
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
        this.gameAudioCapture = new RecordingAudioCaptureService(new RecordingAudioCaptureService.Listener() {
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
                            audioTrack.add(new TimelineClip("game-audio-" + java.util.UUID.randomUUID().toString(),
                                    file.getAbsolutePath(), startMillis, durationMillis));
                        }
                        if (project.isRecording()) {
                            long checkpointAt = System.currentTimeMillis();
                            project.checkpoint(checkpointAt);
                            lastRecoveryCheckpointAt = checkpointAt;
                        }
                        projectStore.save(project, timeline);
                        finishRecoveryMarkerIfIdle();
                        status = "Game audio saved to timeline";
                    } catch (IOException | RuntimeException failure) {
                        status = "Game audio saved; timeline update failed: " + failure.getMessage();
                    }
                }
            }
            public void onAudioFailed(String message) {
                synchronized (RecordingStudioController.this) {
                    status = "Game audio capture failed: " + message;
                    try { finishRecoveryMarkerIfIdle(); }
                    catch (IOException failure) { status += " — Recovery marker retained: " + failure.getMessage(); }
                }
            }
        });
        try {
            int recoveredFrames = RecordingCaptureService.recoverCompletedTemporaryFrames(this.framesDirectory);
            boolean recoveryMarker = this.projectStore.hasRecoveryMarker(project.getId());
            if (recoveryMarker || project.isRecording() || recoveredFrames > 0) {
                project.recoverInterruptedSession(System.currentTimeMillis());
                this.projectStore.save(project, timeline);
                if (recoveryMarker) this.projectStore.clearRecordingActive(project.getId());
                status = recoveredFrames > 0
                        ? "Interrupted session recovered; restored " + recoveredFrames + " completed frame(s)"
                        : "Interrupted session recovered; existing frames are available";
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
            capture.start(now, project.getElapsedMillis(now));
            project.start(now);
            projectStore.save(project, timeline);
            projectStore.markRecordingActive(project);
            sessionStartedInThisRuntime = true;
            lastRecoveryCheckpointAt = now;
            status = "Recording";
            RecordingStudioScene scene = timeline.getActiveScene();
            RecordingStudioSource gameAudio = scene == null ? null
                    : scene.findSource(RecordingStudioSource.Type.GAME_AUDIO);
            if (gameAudio != null && gameAudio.isEnabled()) startGameAudioCapture();
        } catch (IOException failure) {
            long rollbackTime = System.currentTimeMillis();
            capture.stop();
            project.stop(rollbackTime);
            String failureMessage = failure.getMessage();
            try {
                // If the initial checkpoint succeeded but the active marker failed, persist
                // the stopped state so a later launch cannot mistake this failed start for
                // an active recording session.
                projectStore.save(project, timeline);
            } catch (IOException rollbackFailure) {
                failureMessage += "; stopped-state checkpoint also failed: " + rollbackFailure.getMessage();
            }
            status = "Cannot start: " + failureMessage;
        }
    }

    public synchronized void pause() {
        ensureOpen();
        if (!capture.isRecording()) return;
        resumeMicrophoneAfterPause = audioCapture.isRecording();
        resumeGameAudioAfterPause = gameAudioCapture.isRecording();
        audioCapture.stop();
        gameAudioCapture.stop();
        long now = System.currentTimeMillis();
        capture.pause();
        project.pause(now);
        try {
            projectStore.save(project, timeline);
            // Keep the active marker while paused: queued frame/audio writes may still be finalizing.
            // A crash during this period must remain detectable on the next project open.
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
        resumeMicrophoneAfterPause = false;
        resumeGameAudioAfterPause = false;
        audioCapture.stop();
        gameAudioCapture.stop();
        long now = System.currentTimeMillis();
        capture.stop();
        project.stop(now);
        updateCapturedVideoClip(project.getElapsedMillis(now));
        try {
            projectStore.save(project, timeline);
            if (capture.getPendingFrameCount() == 0 && !audioCapture.isFinalizing() && !gameAudioCapture.isFinalizing()) {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
                status = "Stopped — project checkpoint saved";
            } else {
                status = "Stopped — waiting for pending media writes";
            }
        } catch (IOException failure) {
            status = "Stopped; recovery marker retained: " + failure.getMessage();
        }
    }

    /** Keeps the captured frame sequence editable as the default Video-track clip. */
    private void updateCapturedVideoClip(long durationMillis) {
        if (capture.getFrameCount() + capture.getPendingFrameCount() <= 0L) return;
        TimelineTrack videoTrack = null;
        for (TimelineTrack track : timeline.getTracks()) {
            if ("video".equals(track.getId())) { videoTrack = track; break; }
        }
        if (videoTrack == null) {
            videoTrack = new TimelineTrack("video", "Video");
            timeline.addTrack(videoTrack);
        }
        TimelineClip captured = null;
        for (TimelineClip clip : videoTrack.getClips()) {
            if ("@capture".equals(clip.getSource())) { captured = clip; break; }
        }
        long safeDuration = Math.max(1L, durationMillis);
        if (captured == null) {
            videoTrack.add(new TimelineClip("captured-footage", "@capture", 0L, safeDuration));
        } else {
            captured.setDuration(safeDuration);
        }
    }

    public synchronized void startAudioCapture() {
        ensureOpen();
        resumeMicrophoneAfterPause = false;
        if (!capture.isRecording()) {
            status = "Start video recording before capturing audio";
            return;
        }
        RecordingStudioScene activeScene = timeline.getActiveScene();
        RecordingStudioSource microphoneSource = activeScene == null ? null
                : activeScene.findSource(RecordingStudioSource.Type.MICROPHONE);
        if (microphoneSource == null || !microphoneSource.isEnabled()) {
            status = "Enable the Microphone source in the active scene first";
            return;
        }
        try {
            long timelineStart = project.getElapsedMillis(System.currentTimeMillis());
            String sceneDeviceKey = microphoneSource.getAudioDeviceKey();
            if (SYSTEM_DEFAULT_MICROPHONE.equals(sceneDeviceKey)) {
                sceneDeviceKey = "";
            } else if (sceneDeviceKey == null || sceneDeviceKey.isEmpty()) {
                sceneDeviceKey = project.getMicrophoneDeviceKey();
            }
            audioCapture.start(audioDirectory, "audio-" + java.util.UUID.randomUUID().toString() + ".wav",
                    timelineStart, sceneDeviceKey);
            status = "Microphone recording";
        } catch (Exception failure) {
            status = "Cannot start microphone: " + failure.getMessage();
        }
    }

    /** Starts a separate audio input intended for an OS-provided loopback device such as Stereo Mix. */
    public synchronized void startGameAudioCapture() {
        ensureOpen();
        resumeGameAudioAfterPause = false;
        if (!capture.isRecording()) {
            status = "Start video recording before capturing game audio";
            return;
        }
        RecordingStudioScene activeScene = timeline.getActiveScene();
        RecordingStudioSource source = activeScene == null ? null
                : activeScene.findSource(RecordingStudioSource.Type.GAME_AUDIO);
        if (source == null || !source.isEnabled()) {
            status = "Enable the Game Audio source in the active scene first";
            return;
        }
        String deviceKey = source.getAudioDeviceKey();
        if (deviceKey == null || deviceKey.trim().isEmpty()) {
            status = "Select an OS loopback input before starting game audio";
            return;
        }
        try {
            long timelineStart = project.getElapsedMillis(System.currentTimeMillis());
            gameAudioCapture.start(gameAudioDirectory,
                    "game-audio-" + java.util.UUID.randomUUID().toString() + ".wav",
                    timelineStart, deviceKey);
            status = "Game audio recording";
        } catch (Exception failure) {
            status = "Cannot start game audio: " + failure.getMessage();
        }
    }

    public synchronized void stopGameAudioCapture() {
        resumeGameAudioAfterPause = false;
        gameAudioCapture.stop();
        if (gameAudioCapture.isFinalizing()) status = "Finalizing game audio";
    }

    /** Cycles through Java Sound capture inputs; choose an OS loopback device, not a microphone. */
    public synchronized void selectNextGameAudioInput() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()
                || gameAudioCapture.isRecording() || gameAudioCapture.isFinalizing()) {
            status = "Stop recording before changing the game-audio input device";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.GAME_AUDIO);
        if (source == null) {
            status = "The active scene has no Game Audio source";
            return;
        }
        java.util.List<RecordingAudioCaptureService.MicrophoneDevice> devices =
                RecordingAudioCaptureService.getAvailableMicrophones();
        if (devices.isEmpty()) {
            source.setAudioDeviceKey("");
            gameAudioSelectionName = "No loopback input detected";
            status = "No Java Sound capture inputs detected; enable an OS loopback device";
        } else {
            String current = source.getAudioDeviceKey();
            int index = -1;
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getKey().equals(current)) { index = i; break; }
            }
            if (index + 1 >= devices.size()) {
                source.setAudioDeviceKey("");
                gameAudioSelectionName = "No loopback input selected";
            } else {
                RecordingAudioCaptureService.MicrophoneDevice next = devices.get(index + 1);
                source.setAudioDeviceKey(next.getKey());
                gameAudioSelectionName = next.getName();
            }
            status = "Game audio input selected: " + gameAudioSelectionName
                    + " (select an OS loopback device)";
        }
        persistSceneChange(status);
    }

    public synchronized void refreshGameAudioInputs() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()
                || gameAudioCapture.isRecording() || gameAudioCapture.isFinalizing()) {
            status = "Stop recording before refreshing game-audio inputs";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.GAME_AUDIO);
        if (source == null) {
            status = "The active scene has no Game Audio source";
            return;
        }
        java.util.List<RecordingAudioCaptureService.MicrophoneDevice> devices =
                RecordingAudioCaptureService.getAvailableMicrophones();
        String selected = source.getAudioDeviceKey();
        boolean found = false;
        for (RecordingAudioCaptureService.MicrophoneDevice device : devices) {
            if (device.getKey().equals(selected)) {
                gameAudioSelectionName = device.getName();
                found = true;
                break;
            }
        }
        if (!found) {
            source.setAudioDeviceKey("");
            gameAudioSelectionName = "No loopback input selected";
            persistSceneChange("Selected game-audio input is unavailable; choose another OS loopback input");
            return;
        }
        status = "Game audio inputs refreshed: " + devices.size() + " capture input(s)";
    }

    public synchronized String getGameAudioSelectionName() {
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.GAME_AUDIO);
        if (source == null) return "No Game Audio source";
        if (source.getAudioDeviceKey().isEmpty()) return "No loopback input selected";
        for (RecordingAudioCaptureService.MicrophoneDevice device
                : RecordingAudioCaptureService.getAvailableMicrophones()) {
            if (device.getKey().equals(source.getAudioDeviceKey())) return device.getName();
        }
        return "Unavailable";
    }

    public synchronized void startMicrophoneMonitoring() {
        ensureOpen();
        if (!audioCapture.isRecording()) {
            status = "Start microphone recording before enabling live monitoring";
            return;
        }
        try {
            audioCapture.startMonitoring();
            status = "Live microphone monitoring enabled";
        } catch (Exception failure) {
            status = "Cannot start microphone monitoring: " + failure.getMessage();
        }
    }

    public synchronized void stopMicrophoneMonitoring() {
        audioCapture.stopMonitoring();
        status = "Live microphone monitoring disabled";
    }

    public synchronized String getMicrophoneMonitorStatus() {
        return audioCapture.isMonitoring() ? "Live monitoring: ON" : "Live monitoring: OFF";
    }

    public synchronized void stopAudioCapture() {
        resumeMicrophoneAfterPause = false;
        audioCapture.stop();
        if (audioCapture.isFinalizing()) status = "Finalizing microphone audio";
    }

    /** Cycles the active scene's microphone input without changing other scenes. */
    public synchronized void selectNextMicrophone() {
        ensureOpen();
        if (audioCapture.isRecording() || audioCapture.isFinalizing()) {
            status = "Stop microphone capture before changing the input device";
            return;
        }
        RecordingStudioSource microphone = getActiveMicrophoneSource();
        if (microphone == null) {
            status = "The active scene has no Microphone source";
            return;
        }
        java.util.List<RecordingAudioCaptureService.MicrophoneDevice> devices =
                RecordingAudioCaptureService.getAvailableMicrophones();
        String current = microphone.getAudioDeviceKey();
        if ((current == null || current.isEmpty()) && project.getMicrophoneDeviceKey() != null) {
            current = project.getMicrophoneDeviceKey();
        }
        if (SYSTEM_DEFAULT_MICROPHONE.equals(current)) current = "";
        int currentIndex = -1;
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getKey().equals(current)) { currentIndex = i; break; }
        }
        String next;
        if (devices.isEmpty()) {
            next = SYSTEM_DEFAULT_MICROPHONE;
            microphoneSelectionName = "System Default";
            status = "No selectable microphone inputs detected; using system default";
        } else if (currentIndex < 0) {
            next = devices.get(0).getKey();
            microphoneSelectionName = devices.get(0).getName();
            status = "Microphone selected for " + timeline.getActiveScene().getName() + ": " + microphoneSelectionName;
        } else if (currentIndex + 1 >= devices.size()) {
            next = SYSTEM_DEFAULT_MICROPHONE;
            microphoneSelectionName = "System Default";
            status = "Microphone selected for " + timeline.getActiveScene().getName() + ": System Default";
        } else {
            next = devices.get(currentIndex + 1).getKey();
            microphoneSelectionName = devices.get(currentIndex + 1).getName();
            status = "Microphone selected for " + timeline.getActiveScene().getName() + ": " + microphoneSelectionName;
        }
        microphone.setAudioDeviceKey(next);
        try {
            projectStore.save(project, timeline);
        } catch (IOException failure) {
            status = "Microphone changed but scene settings could not be saved: " + failure.getMessage();
        }
    }

    /** Refreshes the active scene's input and falls back to the system default if it was unplugged. */
    public synchronized void refreshMicrophones() {
        ensureOpen();
        if (audioCapture.isRecording() || audioCapture.isFinalizing()) {
            status = "Stop microphone capture before refreshing input devices";
            return;
        }
        RecordingStudioSource microphone = getActiveMicrophoneSource();
        if (microphone == null) {
            status = "The active scene has no Microphone source";
            return;
        }
        java.util.List<RecordingAudioCaptureService.MicrophoneDevice> devices =
                RecordingAudioCaptureService.getAvailableMicrophones();
        String sceneSelection = microphone.getAudioDeviceKey();
        boolean usingProjectFallback = sceneSelection == null || sceneSelection.isEmpty();
        String selected = usingProjectFallback ? project.getMicrophoneDeviceKey() : sceneSelection;
        if (SYSTEM_DEFAULT_MICROPHONE.equals(sceneSelection)) {
            microphoneSelectionName = "System Default";
            status = "Microphone list refreshed: " + devices.size() + " input device(s) detected";
            return;
        }
        if (selected == null || selected.isEmpty()) {
            microphoneSelectionName = "System Default";
            status = "Microphone list refreshed: " + devices.size() + " input device(s) detected";
            return;
        }
        for (RecordingAudioCaptureService.MicrophoneDevice device : devices) {
            if (device.getKey().equals(selected)) {
                microphoneSelectionName = device.getName();
                status = "Microphone list refreshed: " + microphoneSelectionName;
                return;
            }
        }

        // Clear only the unavailable selection. A valid project-level fallback remains usable
        // when a scene-specific device has been unplugged.
        if (usingProjectFallback) {
            project.setMicrophoneDeviceKey("");
        } else {
            microphone.setAudioDeviceKey("");
        }
        String projectFallback = project.getMicrophoneDeviceKey();
        boolean projectFallbackAvailable = projectFallback == null || projectFallback.isEmpty();
        if (!projectFallbackAvailable) {
            for (RecordingAudioCaptureService.MicrophoneDevice device : devices) {
                if (device.getKey().equals(projectFallback)) {
                    projectFallbackAvailable = true;
                    break;
                }
            }
        }
        if (!projectFallbackAvailable) project.setMicrophoneDeviceKey("");
        microphoneSelectionName = resolveMicrophoneName(project.getMicrophoneDeviceKey());
        try {
            projectStore.save(project, timeline);
            status = "Selected microphone is unavailable; fallback changed to " + microphoneSelectionName;
        } catch (IOException failure) {
            status = "Selected microphone is unavailable; fallback could not be saved: " + failure.getMessage();
        }
    }

    public synchronized String getMicrophoneSelectionName() {
        RecordingStudioSource microphone = getActiveMicrophoneSource();
        if (microphone == null) return "No Microphone source";
        String key = microphone.getAudioDeviceKey();
        if (SYSTEM_DEFAULT_MICROPHONE.equals(key)) return "System Default";
        if (key == null || key.isEmpty()) key = project.getMicrophoneDeviceKey();
        return resolveMicrophoneName(key);
    }

    private RecordingStudioSource getActiveMicrophoneSource() {
        RecordingStudioScene scene = timeline.getActiveScene();
        return scene == null ? null : scene.findSource(RecordingStudioSource.Type.MICROPHONE);
    }

    private static String resolveMicrophoneName(String selected) {
        if (selected == null || selected.isEmpty()) return "System Default";
        for (RecordingAudioCaptureService.MicrophoneDevice device
                : RecordingAudioCaptureService.getAvailableMicrophones()) {
            if (device.getKey().equals(selected)) return device.getName();
        }
        return "Unavailable";
    }

    public synchronized String getAudioStatus() {
        RecordingStudioSource microphone = getActiveMicrophoneSource();
        String microphoneStatus = audioCapture.isRecording() ? "Microphone: Recording"
                : audioCapture.isFinalizing() ? "Microphone: Finalizing"
                : microphone == null || !microphone.isEnabled() ? "Microphone: Disabled" : "Microphone: Idle";
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource gameAudio = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.GAME_AUDIO);
        String gameStatus = gameAudioCapture.isRecording() ? "Game audio: Recording"
                : gameAudioCapture.isFinalizing() ? "Game audio: Finalizing"
                : gameAudio == null || !gameAudio.isEnabled() ? "Game audio: Disabled"
                : "Game audio: " + getGameAudioSelectionName();
        return microphoneStatus + " | " + gameStatus;
    }

    public synchronized void startExport(RecordingStudioExportService.Format format) {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before exporting";
            return;
        }
        if (capture.getPendingFrameCount() > 0 || audioCapture.isFinalizing() || gameAudioCapture.isFinalizing()) {
            status = "Wait for pending frame and audio writes before exporting";
            return;
        }
        try {
            double exportFps = 1000.0 / capture.getIntervalMillis();
            exportService.start(format, exportFps, "ffmpeg", exportSettings);
            RecordingStudioExportHud.show(this);
            status = format.name() + " export started";
        } catch (IOException | RuntimeException failure) {
            status = "Cannot start export: " + failure.getMessage();
        }
    }

    public synchronized void cancelExport() {
        if (!exportService.isRunning()) {
            status = "No export is running";
            return;
        }
        exportService.cancel();
        status = "Export cancellation requested";
    }

    public synchronized String getExportStatus() { return exportService.getStatus(); }
    public synchronized boolean isExportRunning() { return exportService.isRunning(); }
    public synchronized int getExportProgressPercent() { return exportService.getProgressPercent(); }
    public synchronized com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportSettings getExportSettings() { return exportSettings; }

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
        if (resumeMicrophoneAfterPause && capture.isRecording()
                && !audioCapture.isRecording() && !audioCapture.isFinalizing()) {
            resumeMicrophoneAfterPause = false;
            startAudioCapture();
        }
        if (resumeGameAudioAfterPause && capture.isRecording()
                && !gameAudioCapture.isRecording() && !gameAudioCapture.isFinalizing()) {
            resumeGameAudioAfterPause = false;
            startGameAudioCapture();
        }
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
                && capture.getPendingFrameCount() == 0 && !audioCapture.isFinalizing() && !gameAudioCapture.isFinalizing()) {
            try {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
                status = "Stopped — project checkpoint saved";
            } catch (IOException failure) {
                status = "Stopped; recovery marker retained: " + failure.getMessage();
            }
        }
    }

    /** Must be invoked from the Minecraft render/HUD event on the client thread. */
    public synchronized void captureFrameTick(long now) {
        if (closed) return;
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) return;
        boolean videoEnabled = false;
        for (RecordingStudioSource source : scene.getSources()) {
            if (isVideoSource(source.getType()) && source.isEnabled()) {
                videoEnabled = true;
                break;
            }
        }
        if (videoEnabled) capture.tick(now);
    }

    private static boolean isVideoSource(RecordingStudioSource.Type type) {
        return type == RecordingStudioSource.Type.GAME_CAPTURE
                || type == RecordingStudioSource.Type.DESKTOP
                || type == RecordingStudioSource.Type.DISPLAY
                || type == RecordingStudioSource.Type.WEBCAM;
    }

    public synchronized java.util.List<DesktopScreenFrameWriter.Display> getAvailableDisplays() {
        java.util.List<DesktopScreenFrameWriter.Display> result =
                new java.util.ArrayList<DesktopScreenFrameWriter.Display>();
        for (DesktopScreenFrameWriter.Display display : DesktopScreenFrameWriter.getDisplays()) {
            if (!display.isPrimary()) result.add(display);
        }
        return java.util.Collections.unmodifiableList(result);
    }

    public synchronized void selectNextDisplay() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before changing the selected display";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.DISPLAY);
        if (source == null) {
            status = "Add a Display source to the active scene first";
            return;
        }
        java.util.List<DesktopScreenFrameWriter.Display> displays = getAvailableDisplays();
        if (displays.isEmpty()) {
            source.setScreenDeviceId("");
            status = "No additional displays detected";
            persistSceneChange(status);
            return;
        }
        int current = -1;
        for (int i = 0; i < displays.size(); i++) {
            if (displays.get(i).getId().equals(source.getScreenDeviceId())) { current = i; break; }
        }
        DesktopScreenFrameWriter.Display next = displays.get((current + 1) % displays.size());
        source.setScreenDeviceId(next.getId());
        persistSceneChange("Display source selected: " + next.getName());
    }

    public synchronized void refreshDisplays() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before refreshing displays";
            return;
        }
        java.util.List<DesktopScreenFrameWriter.Display> displays = getAvailableDisplays();
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.DISPLAY);
        if (source != null && !source.getScreenDeviceId().isEmpty()) {
            boolean found = false;
            for (DesktopScreenFrameWriter.Display display : displays) {
                if (display.getId().equals(source.getScreenDeviceId())) { found = true; break; }
            }
            if (!found) {
                source.setScreenDeviceId("");
                persistSceneChange("Selected display is unavailable; choose another display");
                return;
            }
        }
        status = "Displays refreshed: " + displays.size() + " additional display(s) detected";
    }

    public synchronized String getDisplaySelectionName() {
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.DISPLAY);
        if (source == null) return "No Display source";
        if (source.getScreenDeviceId().isEmpty()) return "No display selected";
        for (DesktopScreenFrameWriter.Display display : getAvailableDisplays()) {
            if (display.getId().equals(source.getScreenDeviceId())) return display.getName();
        }
        return "Unavailable";
    }

    public synchronized RecordingStudioScene getActiveScene() {
        return timeline.getActiveScene();
    }

    public synchronized void selectNextScene() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before changing scenes";
            return;
        }
        java.util.List<RecordingStudioScene> scenes = timeline.getScenes();
        if (scenes.isEmpty()) {
            status = "No scenes are available";
            return;
        }
        int current = -1;
        String activeId = timeline.getActiveSceneId();
        for (int i = 0; i < scenes.size(); i++) {
            if (scenes.get(i).getId().equals(activeId)) { current = i; break; }
        }
        RecordingStudioScene next = scenes.get((current + 1 + scenes.size()) % scenes.size());
        timeline.setActiveScene(next.getId());
        stopMicrophoneIfSceneDisablesIt();
        persistSceneChange("Active scene: " + next.getName());
    }

    public synchronized void createScene() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before creating a scene";
            return;
        }
        String id = "scene-" + java.util.UUID.randomUUID().toString();
        int sceneNumber = 1;
        String sceneName;
        do {
            sceneName = "Scene " + sceneNumber++;
        } while (isSceneNameUsed(sceneName, null));
        RecordingStudioScene scene = RecordingStudioScene.createDefault(id, sceneName);
        timeline.addScene(scene);
        timeline.setActiveScene(scene.getId());
        persistSceneChange("Created " + scene.getName());
    }

    public synchronized void renameActiveScene(String name) {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before renaming a scene";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) {
            status = "No active scene";
            return;
        }
        String requestedName = name == null ? "" : name.trim();
        if (isSceneNameUsed(requestedName, scene.getId())) {
            status = "A scene with that name already exists";
            return;
        }
        try {
            scene.setName(requestedName);
            persistSceneChange("Scene renamed to " + scene.getName());
        } catch (IllegalArgumentException invalid) {
            status = invalid.getMessage();
        }
    }

    private boolean isSceneNameUsed(String name, String excludedSceneId) {
        if (name == null || name.trim().isEmpty()) return false;
        for (RecordingStudioScene scene : timeline.getScenes()) {
            if (excludedSceneId != null && excludedSceneId.equals(scene.getId())) continue;
            if (scene.getName().equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    public synchronized void removeActiveScene() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before removing a scene";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null || !timeline.removeScene(scene.getId())) {
            status = "The last remaining scene cannot be removed";
            return;
        }
        stopMicrophoneIfSceneDisablesIt();
        persistSceneChange("Scene removed");
    }

    /** Reorders a source in the active scene and persists the new source order. */
    public synchronized void moveActiveSceneSource(String sourceId, int direction) {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before reordering scene sources";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) {
            status = "No active scene";
            return;
        }
        if (!scene.moveSource(sourceId, direction)) {
            status = "Source cannot be moved further in that direction";
            return;
        }
        persistSceneChange("Source order updated");
    }

    public synchronized void removeSceneSource(RecordingStudioSource.Type type) {
        ensureOpen();
        if (isVideoSource(type) && (capture.isRecording() || capture.isPaused())) {
            status = "Stop recording before removing a video source";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) {
            status = "No active scene";
            return;
        }
        RecordingStudioSource source = scene.findSource(type);
        if (source == null || !scene.removeSource(source.getId())) {
            status = "Source is not present in the active scene";
            return;
        }
        if (type == RecordingStudioSource.Type.MICROPHONE) {
            resumeMicrophoneAfterPause = false;
            audioCapture.stop();
        } else if (type == RecordingStudioSource.Type.GAME_AUDIO) {
            resumeGameAudioAfterPause = false;
            gameAudioCapture.stop();
        }
        persistSceneChange(source.getName() + " removed from " + scene.getName());
    }

    public synchronized void removeActiveSceneSource(String sourceId) {
        ensureOpen();
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null : scene.findSource(sourceId);
        if (source == null) {
            status = "Select a source before removing it";
            return;
        }
        if (isVideoSource(source.getType()) && (capture.isRecording() || capture.isPaused())) {
            status = "Stop recording before removing a video source"; return;
        }
        if (!scene.removeSource(sourceId)) { status = "Source could not be removed"; return; }
        if (source.getType() == RecordingStudioSource.Type.MICROPHONE) {
            resumeMicrophoneAfterPause = false;
            audioCapture.stop();
        } else if (source.getType() == RecordingStudioSource.Type.GAME_AUDIO) {
            resumeGameAudioAfterPause = false;
            gameAudioCapture.stop();
        }
        persistSceneChange(source.getName() + " removed from " + scene.getName());
    }

    public synchronized void addWebcamSource() {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) { status = "Stop recording before adding a webcam"; return; }
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) { status = "No active scene"; return; }
        int count = 1;
        for (RecordingStudioSource item : scene.getSources()) if (item.getType() == RecordingStudioSource.Type.WEBCAM) count++;
        RecordingStudioSource source = new RecordingStudioSource("webcam-" + java.util.UUID.randomUUID().toString(),
                "Webcam " + count, RecordingStudioSource.Type.WEBCAM, false);
        source.setTransform(72, 70, 25, 25);
        scene.addSource(source);
        while (scene.moveSource(source.getId(), -1)) { }
        persistSceneChange(source.getName() + " added");
    }

    public synchronized void toggleActiveSceneSource(String sourceId) {
        ensureOpen();
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null : scene.findSource(sourceId);
        if (source == null) { status = "Select a source first"; return; }
        if (isVideoSource(source.getType()) && (capture.isRecording() || capture.isPaused())) {
            status = "Stop recording before changing source visibility"; return;
        }
        if (source.getType() == RecordingStudioSource.Type.WEBCAM && !source.isEnabled()
                && (source.getWebcamDeviceName() == null || source.getWebcamDeviceName().isEmpty())) {
            status = "Select a webcam device before enabling this source"; return;
        }
        source.setEnabled(!source.isEnabled());
        if (source.getType() == RecordingStudioSource.Type.MICROPHONE && !source.isEnabled()) {
            resumeMicrophoneAfterPause = false; audioCapture.stop();
        } else if (source.getType() == RecordingStudioSource.Type.GAME_AUDIO && !source.isEnabled()) {
            resumeGameAudioAfterPause = false; gameAudioCapture.stop();
        }
        persistSceneChange(source.getName() + (source.isEnabled() ? " enabled" : " disabled"));
    }

    public synchronized void adjustActiveSceneSource(String sourceId, int dx, int dy, int sizeDelta) {
        ensureOpen();
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null : scene.findSource(sourceId);
        if (source == null || !isVideoSource(source.getType())) { status = "Select a visual source first"; return; }
        source.setTransform(source.getXPercent() + dx, source.getYPercent() + dy,
                source.getWidthPercent() + sizeDelta, source.getHeightPercent() + sizeDelta);
        persistSceneChange(source.getName() + " transform updated");
    }

    public synchronized void resetActiveSceneSourceTransform(String sourceId) {
        ensureOpen();
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null : scene.findSource(sourceId);
        if (source == null || !isVideoSource(source.getType())) { status = "Select a visual source first"; return; }
        source.setTransform(0, 0, 100, 100);
        source.setCrop(0, 0, 0, 0);
        persistSceneChange(source.getName() + " reset to full canvas");
    }

    public synchronized void selectNextWebcamDevice(final String sourceId) {
        ensureOpen();
        if (capture.isRecording() || capture.isPaused()) {
            status = "Stop recording before changing the webcam device";
            return;
        }
        final RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource source = scene == null ? null : scene.findSource(sourceId);
        if (source == null || source.getType() != RecordingStudioSource.Type.WEBCAM) {
            status = "Select a Webcam source first";
            return;
        }
        final String sceneId = scene.getId();
        status = "Scanning DirectShow webcam devices...";
        Thread scanner = new Thread(new Runnable() {
            public void run() {
                try {
                    java.util.List<String> devices = com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioWebcamCapture.listDevices("ffmpeg");
                    synchronized (RecordingStudioController.this) {
                        if (closed) return;
                        RecordingStudioScene active = timeline.getActiveScene();
                        RecordingStudioSource selected = active == null || !sceneId.equals(active.getId())
                                ? null : active.findSource(sourceId);
                        if (selected == null || selected.getType() != RecordingStudioSource.Type.WEBCAM) return;
                        if (devices.isEmpty()) {
                            status = "No DirectShow webcam detected; check FFmpeg and camera permissions";
                            return;
                        }
                        int current = devices.indexOf(selected.getWebcamDeviceName());
                        String next = devices.get((current + 1) % devices.size());
                        selected.setWebcamDeviceName(next);
                        selected.setEnabled(true);
                        persistSceneChange("Webcam selected: " + next);
                    }
                } catch (IOException failure) {
                    synchronized (RecordingStudioController.this) {
                        if (!closed) status = "Cannot enumerate webcams: " + failure.getMessage();
                    }
                }
            }
        }, "Alpha-RecordingStudio-Webcam-Discovery");
        scanner.setDaemon(true);
        scanner.start();
    }

    public synchronized void toggleSceneSource(RecordingStudioSource.Type type) {
        ensureOpen();
        if (type == null) throw new IllegalArgumentException("Source type is required");
        if (isVideoSource(type) && (capture.isRecording() || capture.isPaused())) {
            status = "Stop recording before changing a video source";
            return;
        }
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) {
            status = "No active scene";
            return;
        }
        RecordingStudioSource source = scene.findSource(type);
        if (source == null) {
            String id = type == RecordingStudioSource.Type.GAME_CAPTURE ? "game-capture"
                    : type == RecordingStudioSource.Type.GAME_AUDIO ? "game-audio"
                    : type == RecordingStudioSource.Type.DESKTOP ? "desktop"
                    : type == RecordingStudioSource.Type.DISPLAY ? "display"
                    : type == RecordingStudioSource.Type.WEBCAM ? "webcam" : "microphone";
            String name = type == RecordingStudioSource.Type.GAME_CAPTURE ? "Minecraft Capture"
                    : type == RecordingStudioSource.Type.GAME_AUDIO ? "Game Audio"
                    : type == RecordingStudioSource.Type.DESKTOP ? "Desktop"
                    : type == RecordingStudioSource.Type.DISPLAY ? "Display"
                    : type == RecordingStudioSource.Type.WEBCAM ? "Webcam" : "Microphone";
            source = new RecordingStudioSource(id, name, type, true);
            scene.addSource(source);
        } else {
            source.setEnabled(!source.isEnabled());
        }
        if (type == RecordingStudioSource.Type.MICROPHONE && !source.isEnabled()) {
            resumeMicrophoneAfterPause = false;
            audioCapture.stop();
        } else if (type == RecordingStudioSource.Type.GAME_AUDIO) {
            if (!source.isEnabled()) {
                resumeGameAudioAfterPause = false;
                gameAudioCapture.stop();
            } else if (capture.isRecording()) {
                startGameAudioCapture();
            }
        }
        persistSceneChange(source.getName() + (source.isEnabled() ? " enabled" : " disabled"));
    }

    private void stopMicrophoneIfSceneDisablesIt() {
        RecordingStudioScene scene = timeline.getActiveScene();
        RecordingStudioSource microphone = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.MICROPHONE);
        if ((microphone == null || !microphone.isEnabled()) && audioCapture.isRecording()) {
            audioCapture.stop();
            status = "Microphone stopped because it is disabled in the active scene";
        }
        RecordingStudioSource gameAudio = scene == null ? null
                : scene.findSource(RecordingStudioSource.Type.GAME_AUDIO);
        if ((gameAudio == null || !gameAudio.isEnabled()) && gameAudioCapture.isRecording()) {
            gameAudioCapture.stop();
            status = "Game audio stopped because it is disabled in the active scene";
        }
    }

    private void persistSceneChange(String successStatus) {
        try {
            projectStore.save(project, timeline);
            status = successStatus;
        } catch (IOException failure) {
            status = successStatus + "; could not save scene settings: " + failure.getMessage();
        }
    }

    private void finishRecoveryMarkerIfIdle() throws IOException {
        if (sessionStartedInThisRuntime && !capture.isRecording() && !capture.isPaused()
                && capture.getPendingFrameCount() == 0 && !audioCapture.isFinalizing() && !gameAudioCapture.isFinalizing()) {
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
    public RecordingStudioStorageUsage getStorageUsage() { return storageUsage; }
    public File getFramesDirectory() { return framesDirectory; }

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
                    && !audioCapture.isFinalizing() && !gameAudioCapture.isFinalizing()) {
                projectStore.clearRecordingActive(project.getId());
                sessionStartedInThisRuntime = false;
            }
            status = "Project and timeline saved";
        } catch (IOException failure) {
            status = "Shutdown save failed; recovery marker retained: " + failure.getMessage();
        }
        // Export workers own their inputs and temporary output files; keep accepted exports running
        // after the Studio UI closes. Users can still cancel while the Publishing tab is open.
        capture.shutdown();
        if (sessionStartedInThisRuntime || audioCapture.isFinalizing() || gameAudioCapture.isFinalizing()) {
            final RecordingCaptureService pendingCapture = capture;
            final RecordingStudioProjectStore pendingStore = projectStore;
            final String projectId = project.getId();
            final RecordingAudioCaptureService audioCapture = this.audioCapture;
            Thread finalizer = new Thread(new Runnable() {
                public void run() {
                    try {
                        audioCapture.awaitCompletion();
                        gameAudioCapture.awaitCompletion();
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
