package com.alpharip.staffmod.RecordingStudio.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;

/** Standalone local Recording Studio project. No Build Timelapse types are used. */
public final class RecordingProject {
    private final String id;
    private String name;
    private final long createdAt;
    private long startedAt;
    private long stoppedAt;
    private long elapsedMillis;
    private long captureIntervalMillis = 1000L;
    private long segmentStartedAt;
    private boolean recording;

    public RecordingProject(String name) {
        this(UUID.randomUUID().toString(), name, System.currentTimeMillis());
    }

    private RecordingProject(String id, String name, long createdAt) {
        if (blank(id) || blank(name)) throw new IllegalArgumentException("Project id and name are required");
        this.id = id;
        this.name = name.trim();
        this.createdAt = createdAt;
    }

    public synchronized void start(long now) {
        if (recording) return;
        if (startedAt == 0L) startedAt = now;
        stoppedAt = 0L;
        recording = true;
        segmentStartedAt = now;
    }

    public synchronized void pause(long now) {
        if (!recording) return;
        addElapsed(Math.max(0L, now - segmentStartedAt));
        segmentStartedAt = 0L;
        recording = false;
        stoppedAt = now;
    }

    public synchronized void resume(long now) {
        if (recording) return;
        recording = true;
        stoppedAt = 0L;
        segmentStartedAt = now;
    }

    public synchronized void stop(long now) {
        if (recording) {
            addElapsed(Math.max(0L, now - segmentStartedAt));
            segmentStartedAt = 0L;
            recording = false;
        }
        if (startedAt != 0L) stoppedAt = now;
    }

    /** Restores elapsed active time after an unclean exit, using the persisted active marker. */
    public synchronized void recoverInterruptedSession(long now, long markedAt) {
        if (!recording) return;
        if (markedAt > 0L && now > markedAt) addElapsed(now - markedAt);
        recording = false;
        segmentStartedAt = 0L;
        stoppedAt = now;
    }

    public String getId() { return id; }
    public synchronized String getName() { return name; }
    public synchronized void setName(String value) {
        if (blank(value)) throw new IllegalArgumentException("Project name is required");
        name = value.trim();
    }
    public long getCreatedAt() { return createdAt; }
    public synchronized long getStartedAt() { return startedAt; }
    public synchronized long getStoppedAt() { return stoppedAt; }
    public synchronized boolean isRecording() { return recording; }
    public synchronized long getCaptureIntervalMillis() { return captureIntervalMillis; }
    public synchronized void setCaptureIntervalMillis(long value) {
        if (value < 100L || value > 60000L) throw new IllegalArgumentException("Capture interval must be 100..60000 ms");
        captureIntervalMillis = value;
    }

    /** Elapsed active recording time; paused periods are excluded. */
    public synchronized long getElapsedMillis(long now) {
        if (!recording || segmentStartedAt <= 0L || now <= segmentStartedAt) return elapsedMillis;
        long active = now - segmentStartedAt;
        return elapsedMillis > Long.MAX_VALUE - active ? Long.MAX_VALUE : elapsedMillis + active;
    }

    public synchronized void save(File directory) throws IOException {
        if (directory == null) throw new IllegalArgumentException("directory");
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create Recording Studio project directory");
        }
        Properties properties = new Properties();
        properties.setProperty("format", "1");
        properties.setProperty("id", id);
        properties.setProperty("name", name);
        properties.setProperty("createdAt", Long.toString(createdAt));
        properties.setProperty("startedAt", Long.toString(startedAt));
        properties.setProperty("stoppedAt", Long.toString(stoppedAt));
        properties.setProperty("elapsedMillis", Long.toString(elapsedMillis));
        properties.setProperty("captureIntervalMillis", Long.toString(captureIntervalMillis));
        properties.setProperty("recording", Boolean.toString(recording));

        File temporary = new File(directory, id + ".tmp");
        File destination = new File(directory, id + ".recording");
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(temporary))) {
            properties.store(output, "Alpha Recording Studio project");
        }
        try {
            try {
                Files.move(temporary.toPath(), destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (temporary.exists() && !temporary.delete()) temporary.deleteOnExit();
        }
    }

    public static RecordingProject load(File directory, String id) throws IOException {
        if (blank(id) || !id.matches("[A-Za-z0-9_-]{1,80}")) throw new IOException("Invalid project id");
        Properties properties = new Properties();
        try (InputStream input = new BufferedInputStream(
                new FileInputStream(new File(directory, id + ".recording")))) {
            properties.load(input);
        }
        if (!"1".equals(properties.getProperty("format"))) {
            throw new IOException("Unsupported Recording Studio project format");
        }
        try {
            RecordingProject project = new RecordingProject(properties.getProperty("id"),
                    properties.getProperty("name"), Long.parseLong(properties.getProperty("createdAt")));
            project.startedAt = Long.parseLong(properties.getProperty("startedAt", "0"));
            project.stoppedAt = Long.parseLong(properties.getProperty("stoppedAt", "0"));
            project.elapsedMillis = Math.max(0L, Long.parseLong(properties.getProperty("elapsedMillis", "0")));
            project.setCaptureIntervalMillis(Long.parseLong(properties.getProperty("captureIntervalMillis", "1000")));
            project.recording = Boolean.parseBoolean(properties.getProperty("recording", "false"));
            project.segmentStartedAt = 0L;
            return project;
        } catch (RuntimeException invalid) {
            throw new IOException("Invalid Recording Studio project metadata", invalid);
        }
    }

    private void addElapsed(long value) {
        elapsedMillis = elapsedMillis > Long.MAX_VALUE - value ? Long.MAX_VALUE : elapsedMillis + value;
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
