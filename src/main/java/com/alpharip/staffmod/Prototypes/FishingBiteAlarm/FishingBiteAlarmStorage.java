package com.alpharip.staffmod.fishingbitealarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Local-only settings persistence; no network or Supabase access. */
final class FishingBiteAlarmStorage {
    void load(File file, FishingBiteAlarmSettings settings) {
        if (file == null || settings == null || !file.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            settings.setEnabled(Boolean.parseBoolean(
                    properties.getProperty("enabled", "true")));
            settings.setIntervalSeconds(parseInt(
                    properties.getProperty("intervalSeconds"), 60));
            settings.setVolume(parseFloat(
                    properties.getProperty("volume"), 1.0F));
        } catch (IOException ignored) {
            // Keep defaults if the file cannot be read.
        }
    }

    void save(File file, FishingBiteAlarmSettings settings) {
        if (file == null || settings == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        properties.setProperty("intervalSeconds",
                Integer.toString(settings.getIntervalSeconds()));
        properties.setProperty("volume", Float.toString(settings.getVolume()));

        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "AlphaClient Fishing Bite Alarm settings");
        } catch (IOException ignored) {
            // Runtime settings remain usable if persistence fails.
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
