package com.alpharip.staffmod.RecordingStudio.capture;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Client-thread framebuffer capture with bounded asynchronous image encoding and disk writes. */
public final class RecordingCaptureService {
    public interface FrameWriter {
        FrameData captureFrame() throws Exception;
        void writeFrame(FrameData frame, File output) throws Exception;
    }

    public static final class FrameData {
        private final int width;
        private final int height;
        private final byte[] rgba;

        public FrameData(int width, int height, byte[] rgba) {
            if (width < 1 || height < 1 || rgba == null || (long) width * height * 4L != rgba.length) {
                throw new IllegalArgumentException("Invalid RGBA frame data");
            }
            this.width = width;
            this.height = height;
            this.rgba = rgba;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public byte[] getRgba() { return rgba; }
    }

    private final FrameWriter writer;
    private final RecordingCaptureSettings settings;
    private final File directory;
    private final ThreadPoolExecutor encoder;
    private long nextAt;
    private long nextFileIndex;
    private long capturedFrames;
    private long failedFrames;
    private long droppedFrames;
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
        this.encoder = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(8), runnable -> {
                    Thread thread = new Thread(runnable, "Alpha-RecordingStudio-Encoder");
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
    }

    public synchronized void start(long now) throws IOException {
        ensureOpen();
        if (recording) return;
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create recording output directory");
        }
        initializeNextFileIndex();
        recording = true;
        paused = false;
        nextAt = now;
        lastError = null;
    }

    public synchronized void pause() {
        if (recording) paused = true;
    }

    public synchronized void resume(long now) {
        ensureOpen();
        if (recording && paused) {
            paused = false;
            nextAt = now;
        }
    }

    /** Must be called from the Minecraft client thread because captureFrame reads OpenGL. */
    public void tick(long now) {
        final File output;
        synchronized (this) {
            if (closed || !recording || paused || capturing || now < nextAt) return;
            if (nextFileIndex > 99999999L) {
                nextAt = Long.MAX_VALUE;
                lastError = "Recording frame index limit reached; stop this session before starting another";
                return;
            }
            long interval = settings.getIntervalMillis();
            nextAt = now > Long.MAX_VALUE - interval ? Long.MAX_VALUE : now + interval;
            output = new File(directory, String.format(Locale.ROOT, "frame-%08d.png", nextFileIndex++));
            capturing = true;
        }

        try {
            final FrameData frame = writer.captureFrame();
            encoder.execute(new Runnable() {
                public void run() {
                    try {
                        writer.writeFrame(frame, output);
                        synchronized (RecordingCaptureService.this) {
                            capturedFrames++;
                            lastError = null;
                        }
                    } catch (Exception failure) {
                        synchronized (RecordingCaptureService.this) {
                            failedFrames++;
                            lastError = failure.toString();
                        }
                    }
                }
            });
        } catch (RejectedExecutionException saturated) {
            synchronized (this) {
                droppedFrames++;
                lastError = "Recording encoder queue is full; frame dropped";
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

    public synchronized boolean isRecording() { return recording && !paused; }
    public synchronized boolean isPaused() { return recording && paused; }
    public synchronized long getIntervalMillis() { return settings.getIntervalMillis(); }
    public synchronized void setIntervalMillis(long intervalMillis) {
        ensureOpen();
        settings.setIntervalMillis(intervalMillis);
    }
    public synchronized boolean isCapturing() { return capturing; }
    public synchronized long getFrameCount() { return capturedFrames; }
    public synchronized long getFailedFrameCount() { return failedFrames; }
    public synchronized long getDroppedFrameCount() { return droppedFrames; }
    public synchronized String getLastError() { return lastError; }
    public synchronized int getPendingFrameCount() { return encoder.getQueue().size() + (encoder.getActiveCount() > 0 ? 1 : 0); }

    public void shutdown() {
        synchronized (this) {
            if (closed) return;
            stop();
            closed = true;
        }
        // Drain already accepted frames without blocking the Minecraft client thread.
        encoder.shutdown();
    }

    /** Waits for already accepted frame writes; intended for a non-client finalizer thread. */
    public boolean awaitPendingWrites(long timeout, TimeUnit unit) throws InterruptedException {
        encoder.shutdown();
        return encoder.awaitTermination(timeout, unit);
    }

    private void initializeNextFileIndex() throws IOException {
        if (nextFileIndex != 0L) return;
        File[] existing = directory.listFiles();
        if (existing == null) throw new IOException("Cannot inspect existing frame files; refusing to risk overwriting them");
        long existingFrameCount = 0L;
        for (File file : existing) {
            String name = file.getName();
            if (!file.isFile() || !name.matches("frame-[0-9]{8}\\.png")) continue;
            String number = name.substring(6, name.length() - 4);
            try {
                long index = Long.parseLong(number);
                existingFrameCount++;
                if (index >= 99999999L) throw new IOException("Recording frame index limit reached");
                if (index >= nextFileIndex) nextFileIndex = index + 1L;
            } catch (NumberFormatException ignored) { }
        }
        capturedFrames = existingFrameCount;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Recording capture service is shut down");
    }
}
