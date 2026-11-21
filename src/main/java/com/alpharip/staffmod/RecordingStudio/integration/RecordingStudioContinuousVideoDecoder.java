package com.alpharip.staffmod.RecordingStudio.integration;

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
        if (source == null || !source.isFile()) throw new IOException("Video file is missing");
        errorLog = File.createTempFile("alpha-recording-stream-", ".log");
        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-hide_banner", "-loglevel", "error",
                "-ss", String.format(java.util.Locale.ROOT, "%.3f", Math.max(0.0D, startSeconds)),
                "-re", "-i", source.getAbsolutePath(),
                "-an", "-sn", "-dn",
                "-vf", "scale=" + WIDTH + ":" + HEIGHT
                        + ":force_original_aspect_ratio=decrease,pad=" + WIDTH + ":" + HEIGHT
                        + ":(ow-iw)/2:(oh-ih)/2:black,setsar=1,format=bgr24",
                "-vsync", "0", "-f", "rawvideo", "-pix_fmt", "bgr24", "pipe:1");
        builder.redirectError(errorLog);
        process = builder.start();
        final InputStream input = process.getInputStream();
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
                    if (!closed && process != null) {
                        int exit = process.waitFor();
                        if (exit != 0) failure = "FFmpeg exited with code " + exit;
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } catch (IOException problem) {
                    if (!closed) failure = problem.getMessage();
                } finally {
                    try { input.close(); } catch (IOException ignored) { }
                    outputStream = null;
                    Process finished = process;
                    if (finished != null) {
                        try { finished.waitFor(); }
                        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
                    }
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
