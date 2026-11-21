package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.timeline.TimelineVideoEffect;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Streams decoded MP4 frames from one FFmpeg process for real-time Review playback.
 * The queue is deliberately small and drops stale frames to keep the preview near the playhead.
 */
final class RecordingStudioContinuousVideoDecoder {
    static final int WIDTH = 1280;
    static final int HEIGHT = 720;
    private static final int FRAME_BYTES = WIDTH * HEIGHT * 3;

    private final ArrayBlockingQueue<BufferedImage> frames = new ArrayBlockingQueue<BufferedImage>(2);
    private volatile Process process;
    private volatile Thread reader;
    private volatile InputStream outputStream;
    private volatile String failure;
    private volatile boolean closed;
    private File errorLog;

    RecordingStudioContinuousVideoDecoder(File source, double startSeconds) throws IOException {
        this(source, startSeconds, 1.0D, TimelineVideoEffect.NONE);
    }

    RecordingStudioContinuousVideoDecoder(File source, double startSeconds, double playbackSpeed,
                                           TimelineVideoEffect effect) throws IOException {
        if (source == null || !source.isFile()) throw new IOException("Video file is missing");
        if (playbackSpeed <= 0.0D || playbackSpeed > 100.0D
                || Double.isNaN(playbackSpeed) || Double.isInfinite(playbackSpeed)) {
            throw new IllegalArgumentException("Invalid video playback speed");
        }
        errorLog = File.createTempFile("alpha-recording-stream-", ".log");
        java.util.List<String> command = new java.util.ArrayList<String>();
        java.util.Collections.addAll(command, "ffmpeg", "-hide_banner", "-loglevel", "error",
                "-ss", String.format(java.util.Locale.ROOT, "%.3f", Math.max(0.0D, startSeconds)));
        if (Math.abs(playbackSpeed - 1.0D) < 0.000001D) {
            command.add("-re");
        } else {
            command.add("-readrate");
            command.add(String.format(java.util.Locale.ROOT, "%.6f", playbackSpeed));
        }
        java.util.Collections.addAll(command, "-i", source.getAbsolutePath(),
                "-an", "-sn", "-dn");
        StringBuilder videoFilter = new StringBuilder();
        if (effect != null && !effect.getFfmpegFilter().isEmpty()) {
            videoFilter.append(effect.getFfmpegFilter()).append(',');
        }
        videoFilter.append("scale=").append(WIDTH).append(':').append(HEIGHT)
                .append(":force_original_aspect_ratio=decrease,pad=")
                .append(WIDTH).append(':').append(HEIGHT)
                .append(":(ow-iw)/2:(oh-ih)/2:black,setsar=1,format=bgr24");
        java.util.Collections.addAll(command, "-vf", videoFilter.toString(),
                "-vsync", "0", "-f", "rawvideo", "-pix_fmt", "bgr24", "pipe:1");
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectError(errorLog);
        final Process decoderProcess;
        try {
            decoderProcess = builder.start();
        } catch (IOException startFailure) {
            if (errorLog.exists() && !errorLog.delete()) errorLog.deleteOnExit();
            throw startFailure;
        }
        process = decoderProcess;
        final InputStream input = decoderProcess.getInputStream();
        outputStream = input;
        reader = new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] bytes = new byte[FRAME_BYTES];
                    while (!closed && readFrame(input, bytes)) {
                        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
                        byte[] target = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        System.arraycopy(bytes, 0, target, 0, FRAME_BYTES);
                        if (!frames.offer(image)) {
                            frames.poll();
                            frames.offer(image);
                        }
                    }
                    if (!closed) {
                        int exit = decoderProcess.waitFor();
                        if (exit != 0) failure = "FFmpeg exited with code " + exit;
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } catch (IOException problem) {
                    if (!closed) failure = problem.getMessage();
                } finally {
                    try { input.close(); } catch (IOException ignored) { }
                    outputStream = null;
                    try { decoderProcess.waitFor(); }
                    catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
                    if (errorLog != null && errorLog.exists() && !errorLog.delete()) errorLog.deleteOnExit();
                }
            }
        }, "Alpha-RecordingStudio-ContinuousVideoDecoder");
        reader.setDaemon(true);
        reader.start();
    }

    private static boolean readFrame(InputStream input, byte[] frame) throws IOException {
        int offset = 0;
        while (offset < frame.length) {
            int count = input.read(frame, offset, frame.length - offset);
            if (count < 0) return false;
            if (count == 0) continue;
            offset += count;
        }
        return true;
    }

    BufferedImage pollFrame() {
        return frames.poll();
    }

    String getFailure() {
        return failure;
    }

    boolean isRunning() {
        Process active = process;
        return !closed && active != null && active.isAlive();
    }

    void close() {
        if (closed) return;
        closed = true;
        frames.clear();
        Process active = process;
        if (active != null && active.isAlive()) active.destroyForcibly();
        InputStream input = outputStream;
        if (input != null) {
            try { input.close(); } catch (IOException ignored) { }
        }
        process = null;
    }
}
