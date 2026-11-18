package com.alpharip.staffmod.RecordingStudio.audio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Explicit microphone capture service that writes standalone PCM WAV files off the client thread. */
public final class RecordingAudioCaptureService {
    public interface Listener {
        void onAudioSaved(File file, long timelineStartMillis, long durationMillis);
        void onAudioFailed(String message);
    }

    private static final AudioFormat FORMAT = new AudioFormat(44100.0F, 16, 1, true, false);

    private final Listener listener;
    private TargetDataLine line;
    private Thread writerThread;
    private File temporaryFile;
    private File destinationFile;
    private long timelineStartMillis;
    private boolean recording;

    public RecordingAudioCaptureService(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("Audio capture listener is required");
        this.listener = listener;
    }

    public synchronized void start(File directory, String fileName, long timelineStartMillis) throws Exception {
        if (recording || writerThread != null && writerThread.isAlive()) {
            throw new IllegalStateException("An audio recording is already active or finalizing");
        }
        if (directory == null || fileName == null || !fileName.matches("[A-Za-z0-9_-]{1,80}\\.wav")) {
            throw new IllegalArgumentException("Invalid audio output path");
        }
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create audio output directory");
        }

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
        TargetDataLine openedLine = (TargetDataLine) AudioSystem.getLine(info);
        openedLine.open(FORMAT);
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
        openedLine.start();

        final TargetDataLine captureLine = openedLine;
        final File captureTemp = temp;
        final File captureOutput = output;
        final long captureStart = this.timelineStartMillis;
        writerThread = new Thread(new Runnable() {
            public void run() {
                captureToWave(captureLine, captureTemp, captureOutput, captureStart);
            }
        }, "Alpha-RecordingStudio-Audio");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public synchronized void stop() {
        if (!recording) return;
        recording = false;
        if (line != null) {
            try { line.stop(); } catch (RuntimeException ignored) { }
            try { line.close(); } catch (RuntimeException ignored) { }
        }
    }

    public synchronized boolean isRecording() { return recording; }
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
            AudioInputStream input = new AudioInputStream(captureLine);
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
