package com.alpharip.staffmod.RecordingStudio.audio;

import java.io.File;
import java.io.IOException;

/** Captures the Windows default render endpoint through WASAPI loopback into a PCM WAV file. */
public final class WasapiLoopbackCaptureService {
    public interface Listener {
        void onAudioSaved(File file, long timelineStartMillis, long durationMillis);
        void onAudioFailed(String message);
    }

    private final Listener listener;
    private Thread writerThread;
    private File temporaryFile;
    private File destinationFile;
    private long timelineStartMillis;
    private volatile boolean recording;

    public WasapiLoopbackCaptureService(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("Audio capture listener is required");
        this.listener = listener;
    }

    /** The device key is retained for call compatibility; WASAPI always follows the Windows default output. */
    public synchronized void start(File directory, String fileName, long timelineStartMillis,
                                   String ignoredDeviceKey) throws Exception {
        if (recording || writerThread != null && writerThread.isAlive()) {
            throw new IllegalStateException("Game-audio capture is already active or finalizing");
        }
        if (directory == null || fileName == null || !fileName.matches("[A-Za-z0-9_-]{1,80}\\.wav")) {
            throw new IllegalArgumentException("Invalid game-audio output path");
        }
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create game-audio output directory");
        }

        WasapiNativeBridge.ensureLoaded();
        File output = new File(directory, fileName);
        File temp = new File(directory, fileName + ".tmp");
        if (output.exists() || temp.exists()) throw new IOException("Game-audio output file already exists");

        this.destinationFile = output;
        this.temporaryFile = temp;
        this.timelineStartMillis = Math.max(0L, timelineStartMillis);
        WasapiNativeBridge.nativeReset();
        recording = true;

        final String tempPath = temp.getAbsolutePath();
        final String outputPath = output.getAbsolutePath();
        final long captureStart = this.timelineStartMillis;
        Thread worker = new Thread(new Runnable() {
            public void run() {
                String failureMessage = null;
                long durationMillis = 0L;
                try {
                    durationMillis = WasapiNativeBridge.nativeCapture(tempPath, outputPath);
                    if (durationMillis <= 0L) throw new IOException("No game audio was captured");
                } catch (Exception failure) {
                    failureMessage = failure.toString();
                } catch (LinkageError failure) {
                    failureMessage = "WASAPI native capture failed: " + failure.toString();
                } finally {
                    synchronized (WasapiLoopbackCaptureService.this) {
                        recording = false;
                    }
                }

                try {
                    if (failureMessage == null) listener.onAudioSaved(output, captureStart, durationMillis);
                    else listener.onAudioFailed(failureMessage);
                } finally {
                    synchronized (WasapiLoopbackCaptureService.this) {
                        if (temporaryFile != null && temporaryFile.exists()
                                && !temporaryFile.delete()) temporaryFile.deleteOnExit();
                        writerThread = null;
                        temporaryFile = null;
                        destinationFile = null;
                    }
                }
            }
        }, "Alpha-RecordingStudio-WASAPI");
        worker.setDaemon(true);
        writerThread = worker;
        try {
            worker.start();
        } catch (Throwable failure) {
            recording = false;
            writerThread = null;
            temporaryFile = null;
            destinationFile = null;
            if (temp.exists() && !temp.delete()) temp.deleteOnExit();
            if (failure instanceof Exception) throw (Exception) failure;
            throw (Error) failure;
        }
    }

    public synchronized void stop() {
        recording = false;
        if (writerThread != null && writerThread.isAlive()) {
            try {
                WasapiNativeBridge.nativeStop();
            } catch (LinkageError ignored) {
                // The capture worker reports the native-load failure through its listener.
            }
        }
    }

    public synchronized boolean isRecording() { return recording; }
    public synchronized boolean isFinalizing() {
        return writerThread != null && writerThread.isAlive() && !recording;
    }

    /** Waits for the native capture worker and timeline callback; call from a non-client thread. */
    public void awaitCompletion() throws InterruptedException {
        Thread worker;
        synchronized (this) { worker = writerThread; }
        if (worker != null && worker != Thread.currentThread()) worker.join();
    }
}
