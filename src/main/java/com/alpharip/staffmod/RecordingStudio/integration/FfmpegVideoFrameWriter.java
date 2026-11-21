package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Streams captured RGBA frames directly into FFmpeg and finalizes one MP4 per REC session.
 * Frame acquisition remains delegated to the existing scene compositor.
 */
public final class FfmpegVideoFrameWriter implements RecordingCaptureService.FrameWriter, AutoCloseable {
    private final RecordingCaptureService.FrameWriter source;
    private final File recordingsDirectory;
    private final double framesPerSecond;
    private Process process;
    private OutputStream videoInput;
    private File outputFile;
    private File partialFile;
    private File logFile;
    private int width = -1;
    private int height = -1;
    private long framesWritten;

    public FfmpegVideoFrameWriter(RecordingCaptureService.FrameWriter source, File recordingsDirectory,
                                  double framesPerSecond) {
        if (source == null || recordingsDirectory == null || Double.isNaN(framesPerSecond)
                || Double.isInfinite(framesPerSecond) || framesPerSecond < 1.0D || framesPerSecond > 250.0D) {
            throw new IllegalArgumentException("A frame source, output directory and FPS between 1 and 250 are required");
        }
        this.source = source;
        this.recordingsDirectory = recordingsDirectory;
        this.framesPerSecond = framesPerSecond;
    }

    public synchronized void onCaptureStarted() throws Exception {
        if (process != null || (partialFile != null && partialFile.exists())) {
            throw new IOException("The previous MP4 recording is still being finalized");
        }
        if ((!recordingsDirectory.exists() && !recordingsDirectory.mkdirs()) || !recordingsDirectory.isDirectory()) {
            throw new IOException("Cannot create video recording directory");
        }
        String session = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        outputFile = new File(recordingsDirectory, "recording-" + session + ".mp4");
        partialFile = new File(recordingsDirectory, "recording-" + session + ".partial.mp4");
        logFile = new File(recordingsDirectory, "recording-" + session + ".ffmpeg.log");
        width = -1;
        height = -1;
        framesWritten = 0L;
        source.onCaptureStarted();
    }

    public RecordingCaptureService.FrameData captureFrame() throws Exception {
        return source.captureFrame();
    }

    public synchronized void writeFrame(RecordingCaptureService.FrameData frame, File ignored) throws Exception {
        if (frame == null) throw new IllegalArgumentException("Frame is required");
        if (outputFile == null) throw new IOException("Video recording has not been started");
        if (process == null) startEncoder(frame);
        if (frame.getWidth() != width || frame.getHeight() != height) {
            throw new IOException("Capture dimensions changed during recording; stop and start a new session");
        }
        byte[] rgba = frame.getRgba();
        videoInput.write(rgba);
        framesWritten++;
    }

    private void startEncoder(RecordingCaptureService.FrameData firstFrame) throws IOException {
        width = firstFrame.getWidth();
        height = firstFrame.getHeight();
        java.util.List<String> command = new java.util.ArrayList<String>();
        java.util.Collections.addAll(command, "ffmpeg", "-hide_banner", "-loglevel", "warning",
                "-y", "-f", "rawvideo", "-pixel_format", "rgba", "-video_size",
                width + "x" + height, "-framerate", String.format(java.util.Locale.ROOT, "%.3f", framesPerSecond),
                "-i", "pipe:0", "-an", "-vf", "vflip", "-c:v", "libx264",
                "-preset", "veryfast", "-crf", "18", "-pix_fmt", "yuv420p",
                "-movflags", "+faststart", partialFile.getAbsolutePath());
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectError(logFile);
        try {
            process = builder.start();
            videoInput = process.getOutputStream();
        } catch (IOException failure) {
            throw new IOException("Could not start FFmpeg. Install FFmpeg and make sure ffmpeg is available on PATH.", failure);
        }
    }

    public synchronized void onCaptureStopped() throws Exception {
        Exception failure = null;
        try {
            if (process != null) {
                try {
                    videoInput.close();
                } catch (IOException closeFailure) {
                    failure = closeFailure;
                }
                if (!process.waitFor(90L, TimeUnit.SECONDS)) {
                    process.destroy();
                    if (!process.waitFor(2L, TimeUnit.SECONDS)) process.destroyForcibly();
                    throw new IOException("FFmpeg did not finish writing the MP4 within 90 seconds");
                }
                int exit = process.exitValue();
                process = null;
                videoInput = null;
                if (exit != 0) {
                    throw new IOException("FFmpeg failed with exit code " + exit + "; see " + logFile.getName());
                }
                if (framesWritten < 1L || !partialFile.isFile() || partialFile.length() < 1024L) {
                    throw new IOException("FFmpeg did not produce a valid MP4 recording");
                }
                try {
                    Files.move(partialFile.toPath(), outputFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                    Files.move(partialFile.toPath(), outputFile.toPath());
                }
            }
        } finally {
            try {
                source.onCaptureStopped();
            } catch (Exception sourceFailure) {
                if (failure == null) failure = sourceFailure;
                else failure.addSuppressed(sourceFailure);
            }
            if (process == null && logFile != null && logFile.isFile() && logFile.length() == 0L) {
                if (!logFile.delete()) logFile.deleteOnExit();
            }
            if (failure != null) throw failure;
        }
    }

    public synchronized File getOutputFile() {
        return outputFile;
    }

    public synchronized long getFramesWritten() {
        return framesWritten;
    }

    public synchronized void close() throws Exception {
        if (process != null) onCaptureStopped();
        if (source instanceof AutoCloseable) ((AutoCloseable) source).close();
    }
}
