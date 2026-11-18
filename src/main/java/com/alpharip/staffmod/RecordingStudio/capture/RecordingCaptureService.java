package com.alpharip.staffmod.RecordingStudio.capture;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/** Standalone tick-driven capture scheduler. It does not depend on Build Timelapse. */
public final class RecordingCaptureService {
    public interface FrameWriter {
        void capture(File output) throws Exception;
    }

    private final FrameWriter writer;
    private final RecordingCaptureSettings settings;
    private final File directory;
    private long nextAt;
    private long nextFileIndex;
    private long capturedFrames;
    private long failedFrames;
    private boolean recording;
    private boolean paused;
    private boolean capturing;
    private boolean closed;
    private String lastError;

    public RecordingCaptureService(FrameWriter writer, RecordingCaptureSettings settings, File directory) {
        if (writer == null || settings == null || directory == null) {
            throw new IllegalArgumentException("Writer, settings and output directory are required");
        }
        this.writer = writer;
        this.settings = settings;
        this.directory = directory;
    }

    public synchronized void start(long now) throws IOException {
        ensureOpen();
        if (recording) {
            return;
        }
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create recording output directory");
        }
        recording = true;
        paused = false;
        nextAt = now;
        lastError = null;
    }

    public synchronized void pause() {
        if (recording) {
            paused = true;
        }
    }

    public synchronized void resume(long now) {
        ensureOpen();
        if (recording && paused) {
            paused = false;
            nextAt = now;
        }
    }

    public void tick(long now) {
        final File output;
        synchronized (this) {
            if (closed || !recording || paused || capturing || now < nextAt) {
                return;
            }
            long interval = settings.getIntervalMillis();
            nextAt = now > Long.MAX_VALUE - interval ? Long.MAX_VALUE : now + interval;
            output = new File(directory, String.format(Locale.ROOT, "frame-%08d.png", nextFileIndex++));
            capturing = true;
        }

        try {
            writer.capture(output);
            synchronized (this) {
                capturedFrames++;
                lastError = null;
            }
        } catch (Exception failure) {
            synchronized (this) {
                failedFrames++;
                lastError = failure.toString();
            }
        } finally {
            synchronized (this) {
                capturing = false;
            }
        }
    }

    public synchronized void stop() {
        recording = false;
        paused = false;
    }

    public synchronized boolean isRecording() {
        return recording && !paused;
    }

    public synchronized boolean isPaused() {
        return recording && paused;
    }

    public synchronized boolean isCapturing() {
        return capturing;
    }

    /** Number of frames successfully written by the frame writer. */
    public synchronized long getFrameCount() {
        return capturedFrames;
    }

    public synchronized long getFailedFrameCount() {
        return failedFrames;
    }

    public synchronized String getLastError() {
        return lastError;
    }

    public synchronized void shutdown() {
        if (closed) {
            return;
        }
        stop();
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Recording capture service is shut down");
        }
    }
}
