package com.alpharip.staffmod.rememberchestinventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/** Local-only persistence for chest snapshots. No network or Supabase access. */
public final class ChestMemoryStorage {
    public void save(File file, ChestMemoryManager manager) throws IOException {
        if (file == null || manager == null) throw new IllegalArgumentException("file and manager are required");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create chest memory directory");
        }

        Properties properties = new Properties();
        properties.setProperty("format", "1");
        properties.setProperty("maximum", Integer.toString(manager.getMaximumRememberedChests()));
        properties.setProperty("enabled", Boolean.toString(manager.isEnabled()));
        List<ChestMemoryEntry> entries = manager.getEntries();
        properties.setProperty("count", Integer.toString(entries.size()));

        for (int i = 0; i < entries.size(); i++) {
            ChestMemoryEntry entry = entries.get(i);
            String prefix = "entry." + i + ".";
            properties.setProperty(prefix + "dimension", Integer.toString(entry.getDimensionId()));
            properties.setProperty(prefix + "x", Integer.toString(entry.getX()));
            properties.setProperty(prefix + "y", Integer.toString(entry.getY()));
            properties.setProperty(prefix + "z", Integer.toString(entry.getZ()));
            properties.setProperty(prefix + "name", encode(entry.getChestName()));
            properties.setProperty(prefix + "time", Long.toString(entry.getObservedAt()));
            properties.setProperty(prefix + "items", Integer.toString(entry.getItems().size()));
            for (int n = 0; n < entry.getItems().size(); n++) {
                ChestItemSnapshot item = entry.getItems().get(n);
                String itemPrefix = prefix + "item." + n + ".";
                properties.setProperty(itemPrefix + "id", encode(item.getItemId()));
                properties.setProperty(itemPrefix + "name", encode(item.getDisplayName()));
                properties.setProperty(itemPrefix + "meta", Integer.toString(item.getMetadata()));
                properties.setProperty(itemPrefix + "count", Integer.toString(item.getCount()));
            }
        }

        FileOutputStream output = new FileOutputStream(file);
        try { properties.store(output, "AlphaClient remembered chest inventory"); }
        finally { output.close(); }
    }

    public void load(File file, ChestMemoryManager manager) throws IOException {
        if (file == null || manager == null) throw new IllegalArgumentException("file and manager are required");
        if (!file.isFile()) return;
        Properties properties = new Properties();
        FileInputStream input = new FileInputStream(file);
        try { properties.load(input); }
        finally { input.close(); }

        if (!"1".equals(properties.getProperty("format"))) {
            throw new IOException("Unsupported chest memory format");
        }
        manager.setEnabled(Boolean.parseBoolean(properties.getProperty("enabled", "true")));
        manager.setMaximumRememberedChests(parseInt(properties.getProperty("maximum"), 100));

        int count = bounded(parseInt(properties.getProperty("count"), 0), 0, 10000);
        List<ChestMemoryEntry> entries = new ArrayList<ChestMemoryEntry>();
        for (int i = 0; i < count; i++) {
            String prefix = "entry." + i + ".";
            int dimension = parseInt(properties.getProperty(prefix + "dimension"), 0);
            int x = parseInt(properties.getProperty(prefix + "x"), 0);
            int y = parseInt(properties.getProperty(prefix + "y"), 0);
            int z = parseInt(properties.getProperty(prefix + "z"), 0);
            String name = decode(properties.getProperty(prefix + "name", encode("Chest")));
            long time = parseLong(properties.getProperty(prefix + "time"), 0L);
            int itemCount = bounded(parseInt(properties.getProperty(prefix + "items"), 0), 0, 512);
            List<ChestItemSnapshot> items = new ArrayList<ChestItemSnapshot>();
            for (int n = 0; n < itemCount; n++) {
                String itemPrefix = prefix + "item." + n + ".";
                items.add(new ChestItemSnapshot(
                        decode(properties.getProperty(itemPrefix + "id", "")),
                        decode(properties.getProperty(itemPrefix + "name", "")),
                        parseInt(properties.getProperty(itemPrefix + "meta"), 0),
                        parseInt(properties.getProperty(itemPrefix + "count"), 0)));
            }
            entries.add(new ChestMemoryEntry(dimension, x, y, z, name, time, items));
        }
        manager.replaceAll(entries);
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) throws IOException {
        try { return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8); }
        catch (IllegalArgumentException ex) { throw new IOException("Invalid chest memory text encoding", ex); }
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (RuntimeException ex) { return fallback; }
    }

    private long parseLong(String value, long fallback) {
        try { return Long.parseLong(value); } catch (RuntimeException ex) { return fallback; }
    }

    private int bounded(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}