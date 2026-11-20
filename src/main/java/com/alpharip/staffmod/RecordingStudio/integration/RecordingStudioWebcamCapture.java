package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Windows DirectShow webcam capture using the FFmpeg executable already required for export. */
public final class RecordingStudioWebcamCapture implements AutoCloseable {
    private static final int WIDTH = 640, HEIGHT = 480, FRAME_BYTES = WIDTH * HEIGHT * 4;
    private final String executable;
    private volatile byte[] latestFrame;
    private volatile IOException failure;
    private volatile boolean closed;
    private Process process;
    private Thread reader;
    private String activeDevice;

    public RecordingStudioWebcamCapture(String executable) {
        if (executable == null || executable.trim().isEmpty()) throw new IllegalArgumentException("FFmpeg executable is required");
        this.executable = executable.trim();
    }

    public static List<String> listDevices(String executable) throws IOException {
        File log = File.createTempFile("alpha-webcam-devices-", ".log");
        Process process = null;
        try {
            process = new ProcessBuilder(executable == null || executable.trim().isEmpty() ? "ffmpeg" : executable.trim(),
                    "-hide_banner", "-list_devices", "true", "-f", "dshow", "-i", "dummy")
                    .redirectErrorStream(true).redirectOutput(log).start();
            if (!process.waitFor(5L, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new IOException("Webcam enumeration timed out"); }
            StringBuilder output = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(log), StandardCharsets.UTF_8));
            try { String line; while ((line = in.readLine()) != null) output.append(line).append('\n'); } finally { in.close(); }
            List<String> devices = new ArrayList<String>();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"([^\"]+)\" \\(video\\)").matcher(output);
            while (matcher.find()) if (!devices.contains(matcher.group(1).trim())) devices.add(matcher.group(1).trim());
            return Collections.unmodifiableList(devices);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt(); throw new IOException("Webcam enumeration interrupted", interrupted);
        } catch (IOException unavailable) {
            throw new IOException("FFmpeg DirectShow is unavailable: " + unavailable.getMessage(), unavailable);
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
            if (!log.delete()) log.deleteOnExit();
        }
    }

    public synchronized RecordingCaptureService.FrameData capture(String device) throws IOException {
        if (closed) throw new IOException("Webcam capture is closed");
        if (device == null || device.trim().isEmpty()) throw new IOException("Select a webcam device for this source");
        if (!device.equals(activeDevice) || process == null || !process.isAlive()) start(device);
        byte[] frame = latestFrame;
        if (frame == null) {
            if (failure != null) throw failure;
            throw new IOException("Waiting for the webcam's first frame");
        }
        byte[] bottomUp = new byte[frame.length];
        int stride = WIDTH * 4;
        for (int y = 0; y < HEIGHT; y++) System.arraycopy(frame, y * stride, bottomUp, (HEIGHT - 1 - y) * stride, stride);
        return new RecordingCaptureService.FrameData(WIDTH, HEIGHT, bottomUp);
    }

    private void start(String device) throws IOException {
        stopProcess(); latestFrame = null; failure = null;
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, executable, "-hide_banner", "-loglevel", "error", "-f", "dshow",
                "-video_size", WIDTH + "x" + HEIGHT, "-framerate", "30", "-i", "video=" + device,
                "-an", "-vf", "scale=" + WIDTH + ":" + HEIGHT, "-pix_fmt", "rgba", "-f", "rawvideo", "pipe:1");
        try { process = new ProcessBuilder(args).start(); }
        catch (IOException error) { throw new IOException("Cannot start FFmpeg webcam capture: " + error.getMessage(), error); }
        activeDevice = device;
        final Process active = process;
        reader = new Thread(new Runnable() {
            public void run() {
                try {
                    InputStream stream = active.getInputStream();
                    byte[] frame = new byte[FRAME_BYTES];
                    while (!closed && active == process) {
                        int count = 0;
                        while (count < frame.length) {
                            int read = stream.read(frame, count, frame.length - count);
                            if (read < 0) throw new IOException("Webcam stream ended");
                            count += read;
                        }
                        latestFrame = frame.clone();
                    }
                } catch (IOException stopped) { if (!closed && active == process) failure = stopped; }
            }
        }, "Alpha-RecordingStudio-Webcam");
        reader.setDaemon(true); reader.start();
    }

    private void stopProcess() {
        Process old = process; process = null; activeDevice = null; latestFrame = null;
        if (old != null) {
            old.destroy();
            try { if (!old.waitFor(250L, TimeUnit.MILLISECONDS)) old.destroyForcibly(); }
            catch (InterruptedException interrupted) { old.destroyForcibly(); Thread.currentThread().interrupt(); }
            try { old.getInputStream().close(); } catch (IOException ignored) { }
        }
        reader = null;
    }
    public synchronized void close() { closed = true; stopProcess(); }
}
