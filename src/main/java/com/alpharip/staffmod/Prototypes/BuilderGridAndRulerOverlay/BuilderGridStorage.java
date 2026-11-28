package com.alpharip.staffmod.buildergridandruleroverlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Local-only settings and anchor persistence. No Supabase or network access. */
final class BuilderGridStorage {
    void load(File file, BuilderGridSettings settings, BuilderGridAnchor anchor) {
        if (file == null || settings == null || anchor == null || !file.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            settings.setGridEnabled(Boolean.parseBoolean(
                    properties.getProperty("gridEnabled", "true")));
            settings.setRulerEnabled(Boolean.parseBoolean(
                    properties.getProperty("rulerEnabled", "true")));
            settings.setGridSpacing(parseInt(properties.getProperty("gridSpacing"), 1));
            settings.setAreaRadius(parseInt(properties.getProperty("areaRadius"), 16));
            boolean hasAnchor = Boolean.parseBoolean(
                    properties.getProperty("anchorInitialized", "false"));
            if (hasAnchor) {
                anchor.set(parseInt(properties.getProperty("anchorX"), 0),
                        parseInt(properties.getProperty("anchorY"), 0),
                        parseInt(properties.getProperty("anchorZ"), 0));
            }
        } catch (IOException ignored) {
            // Keep safe defaults if the file cannot be read.
        }
    }

    void save(File file, BuilderGridSettings settings, BuilderGridAnchor anchor) {
        if (file == null || settings == null || anchor == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties properties = new Properties();
        properties.setProperty("gridEnabled", Boolean.toString(settings.isGridEnabled()));
        properties.setProperty("rulerEnabled", Boolean.toString(settings.isRulerEnabled()));
        properties.setProperty("gridSpacing", Integer.toString(settings.getGridSpacing()));
        properties.setProperty("areaRadius", Integer.toString(settings.getAreaRadius()));
        properties.setProperty("anchorInitialized", Boolean.toString(anchor.isInitialized()));
        properties.setProperty("anchorX", Integer.toString(anchor.getX()));
        properties.setProperty("anchorY", Integer.toString(anchor.getY()));
        properties.setProperty("anchorZ", Integer.toString(anchor.getZ()));

        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "AlphaClient Builder Grid & Ruler settings");
        } catch (IOException ignored) {
            // Runtime settings remain usable if persistence fails.
        }
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
