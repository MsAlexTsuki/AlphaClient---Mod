package com.alpharip.staffmod.RecordingStudio.capture;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
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
        default void onCaptureStarted() throws Exception { }
        default void onCaptureStopped() throws Exception { }
        default boolean storesFrameTimestamps() { return true; }
        default File getOutputFile() { return null; }
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
    private long timelineOffsetMillis;
    private long recordingWallStart;
    private long pausedWallMillis;
    private long pauseStartedAt;
    private long capturedFrames;
    private long failedFrames;
    private long droppedFrames;
    private boolean recording;
    private boolean paused;
    private boolean capturing;
    private boolean finalizing;
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
                }, new ThreadPoolExecutor.AbortPolicy()) {
            @Override
            protected void terminated() {
                try {
                    if (RecordingCaptureService.this.writer instanceof AutoCloseable) {
                        ((AutoCloseable) RecordingCaptureService.this.writer).close();
                    }
                } catch (Exception ignored) {
                    // All accepted frames have finished; writer cleanup must not break executor termination.
                } finally {
                    super.terminated();
                }
            }
        };
    }

    /**
     * Restores complete PNG writes left in the writer's temporary-file state by an interrupted process.
     * Invalid or incomplete temporary files are deliberately left untouched for manual inspection.
     */
    public static int recoverCompletedTemporaryFrames(File directory) throws IOException {
        if (directory == null) throw new IllegalArgumentException("Frame directory is required");
        if (!directory.exists()) return 0;
        if (!directory.isDirectory()) throw new IOException("Frame recovery path is not a directory");
        File[] temporaryFrames = directory.listFiles((parent, name) ->
                name.matches("frame-[0-9]{8}\\.png\\.tmp"));
        if (temporaryFrames == null) throw new IOException("Cannot inspect temporary recording frames");

        int recovered = 0;
        for (File temporary : temporaryFrames) {
            if (Files.isSymbolicLink(temporary.toPath()) || !temporary.isFile()) continue;
            String name = temporary.getName();
            File destination = new File(directory, name.substring(0, name.length() - 4));
            if (destination.exists()) continue;
            try {
                BufferedImage image = ImageIO.read(temporary);
                if (image == null) continue;
            } catch (IOException incompleteImage) {
                continue;
            }
            try {
                // Do not use ATOMIC_MOVE here: providers may replace an existing target even
                // without REPLACE_EXISTING when ATOMIC_MOVE is requested.
                Files.move(temporary.toPath(), destination.toPath());
                recovered++;
            } catch (FileAlreadyExistsException alreadyRecovered) {
                // Another recovery pass or process won the destination race; do not overwrite it.
            }
        }
        return recovered;
    }

    public synchronized void start(long now) throws IOException {
        start(now, 0L);
    }

    /** Starts capture with the project's already elapsed active-timeline offset. */
    public synchronized void start(long now, long timelineOffsetMillis) throws IOException {
        ensureOpen();
        if (recording) return;
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create recording output directory");
        }
        if (finalizing) throw new IOException("The previous video is still being finalized");
        try {
            writer.onCaptureStarted();
        } catch (Exception failure) {
            throw new IOException("Cannot initialize recording output: " + failure.getMessage(), failure);
        }
        initializeNextFileIndex();
        recording = true;
        paused = false;
        nextAt = now;
        this.timelineOffsetMillis = Math.max(0L, timelineOffsetMillis);
        recordingWallStart = now;
        pausedWallMillis = 0L;
        pauseStartedAt = 0L;
        lastError = null;
    }

    public synchronized void pause() {
        if (recording && !paused) {
            paused = true;
            pauseStartedAt = System.currentTimeMillis();
        }
    }

    public synchronized void resume(long now) {
        ensureOpen();
        if (recording && paused) {
            if (pauseStartedAt > 0L && now > pauseStartedAt) {
                pausedWallMillis = safeAdd(pausedWallMillis, now - pauseStartedAt);
            }
            pauseStartedAt = 0L;
            paused = false;
            nextAt = now;
        }
    }

    /** Must be called from the Minecraft client thread because captureFrame reads OpenGL. */
    public void tick(long now) {
        final File output;
        final long frameTimestampMillis;
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
            long activeWallMillis = Math.max(0L, now - recordingWallStart - pausedWallMillis);
            frameTimestampMillis = safeAdd(timelineOffsetMillis, activeWallMillis);
            capturing = true;
        }

        try {
            final FrameData frame = writer.captureFrame();
            encoder.execute(new Runnable() {
                public void run() {
                    try {
                        writer.writeFrame(frame, output);
                        String metadataWarning = null;
                        try {
                            if (writer.storesFrameTimestamps()) {
                            RecordingFrameTimestamp.write(output, frameTimestampMillis);
                        }
                        } catch (IOException timestampFailure) {
                            metadataWarning = "Frame saved but timestamp metadata failed: " + timestampFailure.getMessage();
                        }
                        synchronized (RecordingCaptureService.this) {
                            capturedFrames++;
                            lastError = metadataWarning;
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
        if (!recording && !paused) return;
        recording = false;
        paused = false;
        finalizing = true;
        Runnable finish = new Runnable() {
            public void run() {
                try {
                    writer.onCaptureStopped();
                } catch (Exception failure) {
                    synchronized (RecordingCaptureService.this) {
                        lastError = "Video finalization failed: " + failure.getMessage();
                    }
                } finally {
                    synchronized (RecordingCaptureService.this) {
                        finalizing = false;
                    }
                }
            }
        };
        try {
            // Place finalization after every accepted frame write so FFmpeg receives a complete stream.
            encoder.getQueue().put(finish);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            finalizing = false;
            lastError = "Video finalization was interrupted before it could be queued";
        }
    }

    public synchronized boolean isRecording() { return recording && !paused; }
    public synchronized boolean isPaused() { return recording && paused; }
    public synchronized long getIntervalMillis() { return settings.getIntervalMillis(); }
    public synchronized void setIntervalMillis(long intervalMillis) {
        ensureOpen();
        settings.setIntervalMillis(intervalMillis);
    }
    public synchronized boolean isCapturing() { return capturing; }
    public synchronized boolean isFinalizing() { return finalizing; }
    public synchronized File getOutputFile() { return writer.getOutputFile(); }
    public synchronized long getFrameCount() { return capturedFrames; }
    public synchronized long getFailedFrameCount() { return failedFrames; }
    public synchronized long getDroppedFrameCount() { return droppedFrames; }
    public synchronized String getLastError() { return lastError; }
    public synchronized int getPendingFrameCount() {
        return encoder.getQueue().size() + (encoder.getActiveCount() > 0 ? 1 : 0) + (finalizing ? 1 : 0);
    }

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
        if (!writer.storesFrameTimestamps()) {
            capturedFrames = 0L;
            nextFileIndex = 1L;
            return;
        }
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

    private static long safeAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Recording capture service is shut down");
    }
}
