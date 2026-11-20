package com.alpharip.staffmod.RecordingStudio.audio;

import java.io.File;
import java.io.IOException;

/**
 * Uses WASAPI for the default Game Audio source and preserves an explicitly selected
 * Java Sound loopback device as an optional compatibility fallback.
 */
public final class GameAudioCaptureService {
    public interface Listener {
        void onAudioSaved(File file, long timelineStartMillis, long durationMillis);
        void onAudioFailed(String message);
    }

    private final WasapiLoopbackCaptureService wasapi;
    private final RecordingAudioCaptureService javaSound;

    public GameAudioCaptureService(final Listener listener) {
        if (listener == null) throw new IllegalArgumentException("Game-audio capture listener is required");
        this.wasapi = new WasapiLoopbackCaptureService(new WasapiLoopbackCaptureService.Listener() {
            public void onAudioSaved(File file, long startMillis, long durationMillis) {
                listener.onAudioSaved(file, startMillis, durationMillis);
            }
            public void onAudioFailed(String message) {
                listener.onAudioFailed(message);
            }
        });
        this.javaSound = new RecordingAudioCaptureService(new RecordingAudioCaptureService.Listener() {
            public void onAudioSaved(File file, long startMillis, long durationMillis) {
                listener.onAudioSaved(file, startMillis, durationMillis);
            }
            public void onAudioFailed(String message) {
                listener.onAudioFailed(message);
            }
        });
    }

    /**
     * An empty device key uses Windows WASAPI on the default output.
     * A non-empty key explicitly selects the legacy Java Sound capture fallback.
     */
    public synchronized void start(File directory, String fileName, long timelineStartMillis,
                                   String deviceKey) throws Exception {
        if (isRecording() || isFinalizing()) {
            throw new IllegalStateException("Game-audio capture is already active or finalizing");
        }
        if (deviceKey == null || deviceKey.trim().isEmpty()) {
            wasapi.start(directory, fileName, timelineStartMillis, "");
        } else {
            javaSound.start(directory, fileName, timelineStartMillis, deviceKey);
        }
    }

    public synchronized void stop() {
        wasapi.stop();
        javaSound.stop();
    }

    public synchronized boolean isRecording() {
        return wasapi.isRecording() || javaSound.isRecording();
    }

    public synchronized boolean isFinalizing() {
        return wasapi.isFinalizing() || javaSound.isFinalizing();
    }

    /** Waits for both capture backends; call from a non-client thread. */
    public void awaitCompletion() throws InterruptedException {
        wasapi.awaitCompletion();
        javaSound.awaitCompletion();
    }
}
