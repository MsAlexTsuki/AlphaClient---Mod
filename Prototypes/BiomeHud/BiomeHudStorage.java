package com.alpharip.staffmod.BiomeHud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Local-only settings persistence for the Biome HUD prototype. */
final class BiomeHudStorage {
    void load(File file, BiomeHudSettings settings) {
        if (file == null || !file.isFile()) return;

        Properties values = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            values.load(input);
            settings.setEnabled(Boolean.parseBoolean(values.getProperty("enabled", "true")));
            settings.setX(parseInt(values.getProperty("x"), 6));
            settings.setY(parseInt(values.getProperty("y"), 150));
            settings.setScale(parseFloat(values.getProperty("scale"), 1.0F));
        } catch (IOException ignored) {
            // Keep defaults when the local settings file is missing or unreadable.
        }
    }

    void save(File file, BiomeHudSettings settings) {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties values = new Properties();
        values.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        values.setProperty("x", Integer.toString(settings.getX()));
        values.setProperty("y", Integer.toString(settings.getY()));
        values.setProperty("scale", Float.toString(settings.getScale()));
        try (FileOutputStream output = new FileOutputStream(file)) {
            values.store(output, "AlphaClient Biome HUD settings");
        } catch (IOException ignored) {
            // Persistence failure must not interrupt the client.
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
