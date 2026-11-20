package com.alpharip.staffmod.RecordingStudio.core;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Read-only disk usage snapshot for Recording Studio data. */
public final class RecordingStudioStorageUsage {
    private final File root;
    private final Set<String> visitedDirectories = new HashSet<String>();
    private long totalBytes;
    private long projectBytes;
    private long frameBytes;
    private long audioBytes;
    private long gameAudioBytes;
    private long exportBytes;
    private int projectCount;
    private int frameCount;
    private int audioCount;
    private int gameAudioCount;
    private int exportCount;
    private boolean readable = true;
    private volatile boolean scanning;
    private volatile String scanError;

    public RecordingStudioStorageUsage(File recordingStudioRoot) {
        if (recordingStudioRoot == null) throw new IllegalArgumentException("Recording Studio root is required");
        root = recordingStudioRoot;
        refreshAsync();
    }

    public void refreshAsync() {
        synchronized (this) {
            if (scanning) return;
            scanning = true;
            scanError = null;
        }
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    refreshSnapshot();
                } catch (RuntimeException failure) {
                    scanError = failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
                } finally {
                    scanning = false;
                }
            }
        }, "Alpha-RecordingStudio-StorageScan");
        worker.setDaemon(true);
        worker.start();
    }

    public synchronized void refresh() {
        scanning = true;
        scanError = null;
        try {
            refreshSnapshot();
        } finally {
            scanning = false;
        }
    }

    private synchronized void refreshSnapshot() {
        totalBytes = projectBytes = frameBytes = audioBytes = exportBytes = 0L;
        projectCount = frameCount = audioCount = gameAudioCount = exportCount = 0;
        readable = true;
        visitedDirectories.clear();
        File projects = new File(root, "projects");
        File recordings = new File(root, "recordings");
        File exports = new File(root, "exports");
        projectBytes = scan(projects, 0);
        frameBytes = scanRecordingCategory(recordings, "frames", 1);
        audioBytes = scanRecordingCategory(recordings, "audio", 2);
        gameAudioBytes = scanRecordingCategory(recordings, "game-audio", 4);
        exportBytes = scan(exports, 3);
        totalBytes = safeAdd(safeAdd(projectBytes, frameBytes), safeAdd(safeAdd(audioBytes, gameAudioBytes), exportBytes));
    }

    private long scanRecordingCategory(File recordings, String folderName, int category) {
        if (!recordings.isDirectory()) return 0L;
        File[] projects = recordings.listFiles();
        if (projects == null) { readable = false; return 0L; }
        long size = 0L;
        for (File project : projects) {
            if (project.isDirectory()) size = safeAdd(size, scan(new File(project, folderName), category));
        }
        return size;
    }

    private long scan(File file, int category) {
        if (!file.exists()) return 0L;
        if (!file.isDirectory()) {
            if (file.isFile()) {
                count(category);
                return Math.max(0L, file.length());
            }
            return 0L;
        }
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!visitedDirectories.add(canonicalPath)) return 0L;
        } catch (IOException failure) {
            readable = false;
            return 0L;
        }
        File[] children = file.listFiles();
        if (children == null) {
            readable = false;
            return 0L;
        }
        long size = 0L;
        for (File child : children) {
            if (child.isDirectory()) size = safeAdd(size, scan(child, category));
            else if (child.isFile()) {
                count(category);
                size = safeAdd(size, Math.max(0L, child.length()));
            }
        }
        return size;
    }

    private void count(int category) {
        if (category == 0) projectCount++;
        else if (category == 1) frameCount++;
        else if (category == 2) audioCount++;
        else if (category == 3) exportCount++;
        else if (category == 4) gameAudioCount++;
    }

    private static long safeAdd(long a, long b) {
        return a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b;
    }

    public synchronized long getTotalBytes() { return totalBytes; }
    public synchronized long getProjectBytes() { return projectBytes; }
    public synchronized long getFrameBytes() { return frameBytes; }
    public synchronized long getAudioBytes() { return audioBytes; }
    public synchronized long getGameAudioBytes() { return gameAudioBytes; }
    public synchronized long getExportBytes() { return exportBytes; }
    public synchronized int getProjectCount() { return projectCount; }
    public synchronized int getFrameCount() { return frameCount; }
    public synchronized int getAudioCount() { return audioCount; }
    public synchronized int getGameAudioCount() { return gameAudioCount; }
    public synchronized int getExportCount() { return exportCount; }
    public synchronized boolean isReadable() { return readable; }
    public boolean isScanning() { return scanning; }
    public String getScanError() { return scanError; }
    public synchronized String getRootPath() { return root.getAbsolutePath(); }

    public static String formatBytes(long bytes) {
        if (bytes < 1024L) return bytes + " B";
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unit = -1;
        do {
            value /= 1024.0;
            unit++;
        } while (value >= 1024.0 && unit < units.length - 1);
        return String.format(java.util.Locale.ROOT, "%.2f %s", value, units[unit]);
    }
}
