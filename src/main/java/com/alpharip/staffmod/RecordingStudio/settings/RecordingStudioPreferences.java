package com.alpharip.staffmod.RecordingStudio.settings;

import com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportSettings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** User-level Recording Studio preferences, stored separately from individual projects. */
public final class RecordingStudioPreferences {
    private static final String FILE_NAME = "alpharecordingstudio.properties";
    private boolean advancedMode;
    private RecordingStudioExportSettings.Resolution resolution = RecordingStudioExportSettings.Resolution.P1080;
    private RecordingStudioExportSettings.Quality quality = RecordingStudioExportSettings.Quality.HIGH;

    private RecordingStudioPreferences() { }

    public static RecordingStudioPreferences load(File gameDirectory) {
        RecordingStudioPreferences value = new RecordingStudioPreferences();
        if (gameDirectory == null) return value;
        File file = new File(new File(gameDirectory, "config"), FILE_NAME);
        if (!file.isFile()) return value;
        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
            value.advancedMode = Boolean.parseBoolean(properties.getProperty("advancedMode", "false"));
            try {
                value.resolution = RecordingStudioExportSettings.Resolution.valueOf(
                        properties.getProperty("export.resolution", "P1080"));
            } catch (IllegalArgumentException ignored) { }
            try {
                value.quality = RecordingStudioExportSettings.Quality.valueOf(
                        properties.getProperty("export.quality", "HIGH"));
            } catch (IllegalArgumentException ignored) { }
        } catch (Exception ignored) {
            // Invalid or unreadable preferences fall back to safe defaults.
        } finally {
            if (input != null) try { input.close(); } catch (Exception ignored) { }
        }
        return value;
    }

    public synchronized boolean isAdvancedMode() { return advancedMode; }
    public synchronized void setAdvancedMode(boolean value) { advancedMode = value; }
    public synchronized RecordingStudioExportSettings.Resolution getResolution() { return resolution; }
    public synchronized void setResolution(RecordingStudioExportSettings.Resolution value) {
        if (value != null) resolution = value;
    }
    public synchronized RecordingStudioExportSettings.Quality getQuality() { return quality; }
    public synchronized void setQuality(RecordingStudioExportSettings.Quality value) {
        if (value != null) quality = value;
    }

    public synchronized void save(File gameDirectory) {
        if (gameDirectory == null) return;
        File directory = new File(gameDirectory, "config");
        if (!directory.isDirectory() && !directory.mkdirs()) return;
        File target = new File(directory, FILE_NAME);
        File temporary = new File(directory, FILE_NAME + ".tmp");
        Properties properties = new Properties();
        properties.setProperty("advancedMode", Boolean.toString(advancedMode));
        properties.setProperty("export.resolution", resolution.name());
        properties.setProperty("export.quality", quality.name());
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            properties.store(output, "Alpha Recording Studio preferences");
            output.getFD().sync();
            output.close();
            output = null;
            try {
                Files.move(temporary.toPath(), target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
            // Preferences are non-critical; the current session remains usable.
            if (temporary.exists() && !temporary.delete()) temporary.deleteOnExit();
        } finally {
            if (output != null) try { output.close(); } catch (Exception ignored) { }
        }
    }
}
