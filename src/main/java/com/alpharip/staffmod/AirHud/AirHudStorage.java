package com.alpharip.staffmod.AirHud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Local-only settings persistence. */
final class AirHudStorage {
    void load(File file, AirHudSettings settings) {
        if (file == null || !file.isFile()) return;
        Properties values = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            values.load(input);
            settings.setEnabled(Boolean.parseBoolean(values.getProperty("enabled", "true")));
            settings.setShowLabel(Boolean.parseBoolean(values.getProperty("showLabel", "true")));
            try {
                settings.setColorIndex(Integer.parseInt(values.getProperty("colorIndex", "0")));
            } catch (NumberFormatException ignored) {
                settings.setColorIndex(0);
            }
            try {
                settings.setPosition(AirHudSettings.Position.valueOf(
                        values.getProperty("position", "BOTTOM_LEFT")));
            } catch (IllegalArgumentException ignored) {
                settings.setPosition(AirHudSettings.Position.BOTTOM_LEFT);
            }
        } catch (IOException ignored) {
            // A missing or unreadable local settings file leaves safe defaults in place.
        }
    }

    void save(File file, AirHudSettings settings) {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        Properties values = new Properties();
        values.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        values.setProperty("showLabel", Boolean.toString(settings.isShowLabel()));
        values.setProperty("position", settings.getPosition().name());
        values.setProperty("colorIndex", Integer.toString(settings.getColorIndex()));
        try (FileOutputStream output = new FileOutputStream(file)) {
            values.store(output, "AlphaClient Air HUD settings");
        } catch (IOException ignored) {
            // Persistence failure must not interrupt the client.
        }
    }
}
