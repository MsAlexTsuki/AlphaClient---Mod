package com.alpharip.staffmod.chunkborders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Local-only settings persistence. No network or Supabase access. */
final class ChunkBordersStorage {
    void load(File file, ChunkBordersSettings settings) {
        if (file == null || settings == null || !file.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            settings.setEnabled(Boolean.parseBoolean(
                    properties.getProperty("enabled", "false")));
            settings.setStyle(ChunkBorderStyle.fromName(
                    properties.getProperty("style", "default")));
            settings.setRenderRadius(parseInt(
                    properties.getProperty("renderRadius"), 2));
            settings.setShowVerticalPillars(Boolean.parseBoolean(
                    properties.getProperty("showVerticalPillars", "true")));
            settings.setShowHorizontalGrid(Boolean.parseBoolean(
                    properties.getProperty("showHorizontalGrid", "true")));
            settings.setHighlightCurrentChunk(Boolean.parseBoolean(
                    properties.getProperty("highlightCurrentChunk", "true")));
            settings.setOpacity(parseFloat(
                    properties.getProperty("opacity"), 0.75F));
        } catch (IOException ignored) {
            // Keep safe defaults if the file cannot be read.
        }
    }

    void save(File file, ChunkBordersSettings settings) {
        if (file == null || settings == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        properties.setProperty("style", settings.getStyle().name().toLowerCase());
        properties.setProperty("renderRadius", Integer.toString(settings.getRenderRadius()));
        properties.setProperty("showVerticalPillars",
                Boolean.toString(settings.isShowVerticalPillars()));
        properties.setProperty("showHorizontalGrid",
                Boolean.toString(settings.isShowHorizontalGrid()));
        properties.setProperty("highlightCurrentChunk",
                Boolean.toString(settings.isHighlightCurrentChunk()));
        properties.setProperty("opacity", Float.toString(settings.getOpacity()));

        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "AlphaClient Chunk Borders settings");
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
