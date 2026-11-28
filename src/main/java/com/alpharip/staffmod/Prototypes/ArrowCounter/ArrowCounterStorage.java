package com.alpharip.staffmod.ArrowCounter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Persists ArrowCounter HUD settings locally. */
final class ArrowCounterStorage {
    void load(File file, ArrowCounterSettings settings) {
        if (file == null || !file.isFile()) return;

        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
            settings.setEnabled(Boolean.parseBoolean(properties.getProperty("enabled", "true")));
            settings.setX(parseInt(properties.getProperty("x"), 6));
            settings.setY(parseInt(properties.getProperty("y"), 170));
            settings.setScale(parseFloat(properties.getProperty("scale"), 1.0F));
        } catch (IOException ignored) {
            // Keep defaults when the local settings file cannot be read.
        } finally {
            if (input != null) {
                try { input.close(); } catch (IOException ignored) { }
            }
        }
    }

    void save(File file, ArrowCounterSettings settings) {
        if (file == null || settings == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        properties.setProperty("x", Integer.toString(settings.getX()));
        properties.setProperty("y", Integer.toString(settings.getY()));
        properties.setProperty("scale", Float.toString(settings.getScale()));

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            properties.store(output, "ArrowCounter settings");
        } catch (IOException ignored) {
            // Settings remain available for the current session.
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
