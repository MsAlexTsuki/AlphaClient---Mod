package com.alpharip.staffmod.ChestItemTotal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Saves ChestItemTotal display settings locally; no remote service is used. */
final class ChestItemTotalStorage {
    void load(File file, ChestItemTotalSettings settings) {
        if (file == null || !file.isFile()) return;
        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
            settings.setEnabled(Boolean.parseBoolean(
                    properties.getProperty("enabled", "true")));
            settings.setX(parseInt(properties.getProperty("x"), 8));
            settings.setY(parseInt(properties.getProperty("y"), 8));
        } catch (IOException ignored) {
            // Keep defaults when local settings cannot be read.
        } finally {
            if (input != null) {
                try { input.close(); } catch (IOException ignored) { }
            }
        }
    }

    void save(File file, ChestItemTotalSettings settings) {
        if (file == null || settings == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        properties.setProperty("x", Integer.toString(settings.getX()));
        properties.setProperty("y", Integer.toString(settings.getY()));

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            properties.store(output, "ChestItemTotal settings");
        } catch (IOException ignored) {
            // Settings remain active for the current session.
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
}
