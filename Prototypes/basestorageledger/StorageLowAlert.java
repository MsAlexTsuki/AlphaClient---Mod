package com.alpharip.staffmod.basestorageledger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Local minimum-stock thresholds. This class has no network or Supabase dependency. */
public final class StorageLowAlert {
    private final Map<String, Integer> minimums = new LinkedHashMap<String, Integer>();

    public synchronized void setMinimum(String itemKey, int minimum) {
        if (itemKey == null || itemKey.trim().isEmpty()) {
            throw new IllegalArgumentException("itemKey is required");
        }
        if (minimum < 1) throw new IllegalArgumentException("minimum must be at least 1");
        minimums.put(itemKey, minimum);
    }

    public synchronized void remove(String itemKey) {
        if (itemKey != null) minimums.remove(itemKey);
    }

    public synchronized int getMinimum(String itemKey) {
        Integer value = minimums.get(itemKey);
        return value == null ? 0 : value;
    }

    public synchronized boolean hasAlert(String itemKey) {
        return minimums.containsKey(itemKey);
    }

    public synchronized boolean isLow(StorageLedgerItem item) {
        if (item == null || !minimums.containsKey(item.getKey())) return false;
        return item.getTotalCount() < minimums.get(item.getKey());
    }

    public synchronized Map<String, Integer> getMinimums() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(minimums));
    }

    public synchronized void save(File file) throws IOException {
        requireFile(file);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create Base Storage Ledger settings directory");
        }

        Properties properties = new Properties();
        properties.setProperty("format", "1");
        properties.setProperty("count", Integer.toString(minimums.size()));
        int index = 0;
        for (Map.Entry<String, Integer> entry : minimums.entrySet()) {
            String prefix = "alert." + index++ + ".";
            properties.setProperty(prefix + "key", encode(entry.getKey()));
            properties.setProperty(prefix + "minimum", Integer.toString(entry.getValue()));
        }

        FileOutputStream output = new FileOutputStream(file);
        try {
            properties.store(output, "AlphaClient Base Storage Ledger alerts");
        } finally {
            output.close();
        }
    }

    public synchronized void load(File file) throws IOException {
        requireFile(file);
        if (!file.isFile()) return;

        Properties properties = new Properties();
        FileInputStream input = new FileInputStream(file);
        try {
            properties.load(input);
        } finally {
            input.close();
        }

        if (!"1".equals(properties.getProperty("format"))) {
            throw new IOException("Unsupported Base Storage Ledger settings format");
        }

        Map<String, Integer> loaded = new LinkedHashMap<String, Integer>();
        int count = bounded(parseInt(properties.getProperty("count"), 0), 0, 10000);
        for (int i = 0; i < count; i++) {
            String prefix = "alert." + i + ".";
            String key = decode(properties.getProperty(prefix + "key", ""));
            int minimum = parseInt(properties.getProperty(prefix + "minimum"), 0);
            if (!key.isEmpty() && minimum > 0) loaded.put(key, minimum);
        }
        minimums.clear();
        minimums.putAll(loaded);
    }

    private static void requireFile(File file) {
        if (file == null) throw new IllegalArgumentException("file is required");
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) throws IOException {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid alert key encoding", ex);
        }
    }

    private static int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (RuntimeException ex) { return fallback; }
    }

    private static int bounded(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
