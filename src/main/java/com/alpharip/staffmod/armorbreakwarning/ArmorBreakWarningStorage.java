package com.alpharip.staffmod.armorbreakwarning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Local-only settings persistence. */
final class ArmorBreakWarningStorage {
    void load(File file, ArmorBreakWarningSettings settings) {
        if (file == null || !file.isFile()) return;

        Properties values = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            values.load(input);
            settings.setEnabled(Boolean.parseBoolean(values.getProperty("enabled", "true")));
            try {
                settings.setThresholdPercent(Integer.parseInt(values.getProperty("thresholdPercent", "10")));
            } catch (NumberFormatException ignored) {
                settings.setThresholdPercent(10);
            }
        } catch (IOException ignored) {
            // Keep defaults when the local settings file is missing or unreadable.
        }
    }

    void save(File file, ArmorBreakWarningSettings settings) {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties values = new Properties();
        values.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        values.setProperty("thresholdPercent", Integer.toString(settings.getThresholdPercent()));
        try (FileOutputStream output = new FileOutputStream(file)) {
            values.store(output, "AlphaClient Armor Break Warning settings");
        } catch (IOException ignored) {
            // Persistence failure must not interrupt the client.
        }
    }
}
