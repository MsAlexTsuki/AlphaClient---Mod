package com.alpharip.staffmod.autosave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Local settings persistence for AutoSave; no remote or Supabase storage. */
final class AutoSaveStorage {
    void load(File file, AutoSaveSettings settings) {
        if (file == null || settings == null || !file.isFile()) return;
        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
            settings.setEnabled(Boolean.parseBoolean(properties.getProperty("enabled", "true")));
            settings.setShowSaveState(Boolean.parseBoolean(properties.getProperty("showSaveState", "true")));
            settings.setRecentSaveWindowSeconds(parseInt(properties.getProperty("recentSaveWindowSeconds"), 10));
            settings.setX(parseInt(properties.getProperty("x"), 6));
            settings.setY(parseInt(properties.getProperty("y"), 6));
            settings.setScale(parseFloat(properties.getProperty("scale"), 1.0F));
        } catch (IOException ignored) {
            // Preserve defaults if the settings file is unavailable.
        } finally {
            if (input != null) {
                try { input.close(); } catch (IOException ignored) { }
            }
        }
    }

    void save(File file, AutoSaveSettings settings) {
        if (file == null || settings == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        properties.setProperty("showSaveState", Boolean.toString(settings.isShowSaveState()));
        properties.setProperty("recentSaveWindowSeconds", Integer.toString(settings.getRecentSaveWindowSeconds()));
        properties.setProperty("x", Integer.toString(settings.getX()));
        properties.setProperty("y", Integer.toString(settings.getY()));
        properties.setProperty("scale", Float.toString(settings.getScale()));

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            properties.store(output, "AlphaClient AutoSave settings");
        } catch (IOException ignored) {
            // Runtime settings remain available if disk persistence fails.
        } finally {
            if (output != null) {
                try { output.close(); } catch (IOException ignored) { }
            }
        }
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private float parseFloat(String value, float fallback) {
        try { return Float.parseFloat(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
