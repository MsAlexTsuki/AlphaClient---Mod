package com.alpharip.staffmod.RecordingStudio.audio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** Explicit microphone capture service that writes standalone PCM WAV files off the client thread. */
public final class RecordingAudioCaptureService {
    public interface Listener {
        void onAudioSaved(File file, long timelineStartMillis, long durationMillis);
        void onAudioFailed(String message);
    }

    public static final class MicrophoneDevice {
        private final Mixer.Info info;

        private MicrophoneDevice(Mixer.Info info) { this.info = info; }

        public String getName() { return info.getName(); }

        public String getKey() {
            return info.getName() + "|" + info.getVendor() + "|" + info.getDescription() + "|" + info.getVersion();
        }
    }

    private static final AudioFormat FORMAT = new AudioFormat(44100.0F, 16, 1, true, false);

    private final Listener listener;
    private TargetDataLine line;
    private Thread writerThread;
    private File temporaryFile;
    private File destinationFile;
    private long timelineStartMillis;
    private boolean recording;
    private volatile boolean monitoring;
    private SourceDataLine monitorLine;
    private Thread monitorThread;
    private BlockingQueue<byte[]> monitorQueue;

    /** Lists only Java Sound inputs whose descriptors explicitly identify loopback capture. */
    public static List<MicrophoneDevice> getAvailableLoopbackDevices() {
        List<MicrophoneDevice> devices = new ArrayList<MicrophoneDevice>();
        for (MicrophoneDevice device : getAvailableMicrophones()) {
            String descriptor = (device.info.getName() + " " + device.info.getDescription())
                    .toLowerCase(java.util.Locale.ROOT);
            if (descriptor.contains("loopback") || descriptor.contains("stereo mix")
                    || descriptor.contains("what u hear") || descriptor.contains("wave out mix")
                    || descriptor.contains("monitor of") || descriptor.contains("cable output")
                    || descriptor.contains("virtual cable")) {
                devices.add(device);
            }
        }
        return Collections.unmodifiableList(devices);
    }

    public RecordingAudioCaptureService(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("Audio capture listener is required");
        this.listener = listener;
    }

    /** Lists mixers that advertise support for microphone capture using the Studio PCM format. */
    public static List<MicrophoneDevice> getAvailableMicrophones() {
        List<MicrophoneDevice> devices = new ArrayList<MicrophoneDevice>();
        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, FORMAT);
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            try {
                if (AudioSystem.getMixer(info).isLineSupported(lineInfo)) {
                    devices.add(new MicrophoneDevice(info));
                }
            } catch (RuntimeException ignored) {
                // A device can disappear while the operating system's mixer list is being read.
            }
        }
        return Collections.unmodifiableList(devices);
    }

    public synchronized void start(File directory, String fileName, long timelineStartMillis) throws Exception {
        start(directory, fileName, timelineStartMillis, "");
    }

    /** An empty device key uses the operating system's default microphone. */
    public synchronized void start(File directory, String fileName, long timelineStartMillis,
                                   String microphoneDeviceKey) throws Exception {
        if (recording || writerThread != null && writerThread.isAlive()) {
            throw new IllegalStateException("An audio recording is already active or finalizing");
        }
        if (directory == null || fileName == null || !fileName.matches("[A-Za-z0-9_-]{1,80}\\.wav")) {
            throw new IllegalArgumentException("Invalid audio output path");
        }
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create audio output directory");
        }

        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, FORMAT);
        TargetDataLine openedLine;
        if (microphoneDeviceKey == null || microphoneDeviceKey.trim().isEmpty()) {
            openedLine = (TargetDataLine) AudioSystem.getLine(lineInfo);
        } else {
            openedLine = null;
            for (MicrophoneDevice device : getAvailableMicrophones()) {
                if (device.getKey().equals(microphoneDeviceKey)) {
                    openedLine = (TargetDataLine) AudioSystem.getMixer(device.info).getLine(lineInfo);
                    break;
                }
            }
            if (openedLine == null) throw new IOException("Selected microphone is no longer available");
        }
        try {
            openedLine.open(FORMAT);
        } catch (Exception failure) {
            try { openedLine.close(); } catch (RuntimeException ignored) { }
            throw failure;
        }
        File output = new File(directory, fileName);
        File temp = new File(directory, fileName + ".tmp");
        if (output.exists() || temp.exists()) {
            openedLine.close();
            throw new IOException("Audio output file already exists");
        }

        this.line = openedLine;
        this.destinationFile = output;
        this.temporaryFile = temp;
        this.timelineStartMillis = Math.max(0L, timelineStartMillis);
        this.recording = true;

        final TargetDataLine captureLine = openedLine;
        final File captureTemp = temp;
        final File captureOutput = output;
        final long captureStart = this.timelineStartMillis;
        try {
            openedLine.start();
            Thread worker = new Thread(new Runnable() {
                public void run() {
                    captureToWave(captureLine, captureTemp, captureOutput, captureStart);
                }
            }, "Alpha-RecordingStudio-Audio");
            worker.setDaemon(true);
            writerThread = worker;
            worker.start();
        } catch (Throwable failure) {
            recording = false;
            line = null;
            writerThread = null;
            temporaryFile = null;
            destinationFile = null;
            try { openedLine.stop(); } catch (RuntimeException ignored) { }
            try { openedLine.close(); } catch (RuntimeException ignored) { }
            if (temp.exists() && !temp.delete()) temp.deleteOnExit();
            if (failure instanceof Exception) throw (Exception) failure;
            throw (Error) failure;
        }
    }

    public synchronized void stop() {
        stopMonitoring();
        if (!recording) return;
        recording = false;
        if (line != null) {
            try { line.stop(); } catch (RuntimeException ignored) { }
            try { line.close(); } catch (RuntimeException ignored) { }
        }
    }

    public synchronized boolean isRecording() { return recording; }
    public synchronized boolean isMonitoring() { return monitoring; }

    /** Mirrors the active microphone capture to the system's default audio output. */
    public synchronized void startMonitoring() throws Exception {
        if (!recording || line == null || !line.isOpen()) {
            throw new IllegalStateException("Start microphone capture before enabling live monitoring");
        }
        if (monitoring) return;
        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, FORMAT);
        SourceDataLine output = (SourceDataLine) AudioSystem.getLine(outputInfo);
        try {
            output.open(FORMAT);
            output.start();
        } catch (Exception failure) {
            try { output.close(); } catch (RuntimeException ignored) { }
            throw failure;
        }
        final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(24);
        monitorLine = output;
        monitorQueue = queue;
        monitoring = true;
        Thread worker = new Thread(new Runnable() {
            public void run() { monitorToOutput(output, queue); }
        }, "Alpha-RecordingStudio-Microphone-Monitor");
        worker.setDaemon(true);
        monitorThread = worker;
        worker.start();
    }

    public synchronized void stopMonitoring() {
        monitoring = false;
        BlockingQueue<byte[]> queue = monitorQueue;
        if (queue != null) {
            queue.clear();
            queue.offer(new byte[0]);
        }
        SourceDataLine output = monitorLine;
        monitorLine = null;
        monitorQueue = null;
        monitorThread = null;
        if (output != null) {
            try { output.flush(); } catch (RuntimeException ignored) { }
            try { output.stop(); } catch (RuntimeException ignored) { }
            try { output.close(); } catch (RuntimeException ignored) { }
        }
    }

    private void monitorToOutput(SourceDataLine output, BlockingQueue<byte[]> queue) {
        try {
            while (monitoring && output.isOpen()) {
                byte[] chunk = queue.poll(200L, TimeUnit.MILLISECONDS);
                if (chunk == null) continue;
                if (chunk.length == 0) break;
                output.write(chunk, 0, chunk.length);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ignored) {
            // Monitoring is optional; output-device failures must not interrupt WAV capture.
        } finally {
            if (output.isOpen()) {
                try { output.stop(); } catch (RuntimeException ignored) { }
                try { output.close(); } catch (RuntimeException ignored) { }
            }
        }
    }

    private void publishMonitorChunk(byte[] bytes, int offset, int length) {
        BlockingQueue<byte[]> queue;
        synchronized (this) {
            if (!monitoring) return;
            queue = monitorQueue;
        }
        if (queue != null && length > 0) {
            // Monitoring is best-effort and bounded; it must never stall the recording writer.
            queue.offer(Arrays.copyOfRange(bytes, offset, offset + length));
        }
    }
    public synchronized boolean isFinalizing() { return writerThread != null && writerThread.isAlive() && !recording; }

    /** Waits for the WAV writer and completion callback; call from a non-client thread. */
    public void awaitCompletion() throws InterruptedException {
        Thread worker;
        synchronized (this) { worker = writerThread; }
        if (worker != null && worker != Thread.currentThread()) worker.join();
    }

    private void captureToWave(TargetDataLine captureLine, File temp, File output, long startMillis) {
        String failureMessage = null;
        long bytesWritten = 0L;
        try {
            java.io.InputStream captureInput = new java.io.InputStream() {
                public int read() throws IOException {
                    byte[] one = new byte[1];
                    int count = read(one, 0, 1);
                    return count < 0 ? -1 : one[0] & 0xFF;
                }
                public int read(byte[] buffer, int offset, int length) throws IOException {
                    if (!captureLine.isOpen()) return -1;
                    int count = captureLine.read(buffer, offset, length);
                    if (count > 0) publishMonitorChunk(buffer, offset, count);
                    if (count <= 0 && !captureLine.isOpen()) return -1;
                    return count;
                }
            };
            AudioInputStream input = new AudioInputStream(captureInput, FORMAT, AudioSystem.NOT_SPECIFIED);
            try {
                bytesWritten = AudioSystem.write(input, AudioFileFormat.Type.WAVE, temp);
            } finally {
                try { input.close(); } catch (IOException ignored) { }
            }
            if (bytesWritten <= 44L) throw new IOException("No microphone audio was captured");
            try {
                Files.move(temp.toPath(), output.toPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                Files.move(temp.toPath(), output.toPath());
            }
        } catch (Exception failure) {
            failureMessage = failure.toString();
            if (temp.exists() && !temp.delete()) temp.deleteOnExit();
        }

        long dataBytes = failureMessage == null ? Math.max(0L, output.length() - 44L) : 0L;
        long frameSize = FORMAT.getFrameSize();
        long durationMillis = dataBytes <= 0L ? 0L
                : (dataBytes / frameSize) * 1000L / (long) FORMAT.getSampleRate();
        stopMonitoring();
        synchronized (this) {
            recording = false;
            line = null;
            temporaryFile = null;
            destinationFile = null;
        }
        try {
            if (failureMessage == null) listener.onAudioSaved(output, startMillis, durationMillis);
            else listener.onAudioFailed(failureMessage);
        } finally {
            synchronized (this) { writerThread = null; }
        }
    }
}
