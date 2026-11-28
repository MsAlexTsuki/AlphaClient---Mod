package com.alpharip.staffmod.TimeTill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Reads and writes TimeTill settings locally; no account or Supabase integration. */
final class TimeTillStorage {
    void load(File file, TimeTillSettings settings) {
        if (file == null || !file.isFile()) return;

        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
            settings.setEnabled(Boolean.parseBoolean(properties.getProperty("enabled", "true")));
            settings.setTarget(parseTarget(properties.getProperty("target", "SUNSET")));
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

    void save(File file, TimeTillSettings settings) {
        if (file == null || settings == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        properties.setProperty("target", settings.getTarget().name());
        properties.setProperty("x", Integer.toString(settings.getX()));
        properties.setProperty("y", Integer.toString(settings.getY()));
        properties.setProperty("scale", Float.toString(settings.getScale()));

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            properties.store(output, "TimeTill settings");
        } catch (IOException ignored) {
            // Settings remain available for the current session.
        } finally {
            if (output != null) {
                try { output.close(); } catch (IOException ignored) { }
            }
        }
    }

    private TimeTillSettings.Target parseTarget(String value) {
        try {
            return TimeTillSettings.Target.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return TimeTillSettings.Target.SUNSET;
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
