package com.alpharip.staffmod.indicators.bow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Local Bow Charge Indicator prototype.
 * A future Forge adapter supplies draw ticks; a future HUD renderer consumes snapshots.
 */
public final class BowChargeIndicator {
    public static final String NAME = "Bow Charge Indicator";
    public static final String CATEGORY = "Indicators";
    public static final String STATUS = "PROTOTYPE";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private final BowChargeConfig config;
    private final Map<String, String> options = new LinkedHashMap<String, String>();
    private final List<String> records = new ArrayList<String>();
    private boolean enabled = true;
    private BowChargeSnapshot latestSnapshot;

    public BowChargeIndicator() { this(new BowChargeConfig()); }

    public BowChargeIndicator(BowChargeConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null.");
        this.config = config;
    }

    public synchronized boolean isEnabled() { return enabled; }

    public synchronized void setEnabled(boolean value) {
        enabled = value;
        if (!value) latestSnapshot = null;
    }

    public synchronized void setOption(String key, String value) {
        if (key == null || key.trim().isEmpty()) return;
        if (value == null) options.remove(key);
        else options.put(key, value);
    }

    public synchronized String getOption(String key, String fallback) {
        String value = options.get(key);
        return value == null ? fallback : value;
    }

    public synchronized Map<String, String> getOptions() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(options));
    }

    public synchronized BowChargeSnapshot updateCharge(int drawTicks) {
        return updateCharge(drawTicks, System.currentTimeMillis());
    }

    /** Explicit timestamp overload supports deterministic tests and replay adapters. */
    public synchronized BowChargeSnapshot updateCharge(int drawTicks, long timestampMillis) {
        if (!enabled) {
            latestSnapshot = null;
            return null;
        }
        int safeTicks = Math.max(0, drawTicks);
        int percent = BowChargeCalculator.calculatePercent(safeTicks, config.getFullChargeTicks());
        latestSnapshot = new BowChargeSnapshot(safeTicks, percent,
                percent >= config.getReadyPercent(), timestampMillis);
        return latestSnapshot;
    }

    public synchronized BowChargeSnapshot getSnapshot() { return latestSnapshot; }

    public synchronized int getChargePercent(int ticks) {
        return BowChargeCalculator.calculatePercent(Math.max(0, ticks), config.getFullChargeTicks());
    }

    public synchronized void clear() { latestSnapshot = null; }

    public synchronized void record(String value) {
        if (value != null) records.add(value);
    }

    public synchronized List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public synchronized List<String> searchRecords(String query) {
        if (query == null || query.trim().isEmpty()) return getRecords();
        String normalized = query.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<String>();
        for (String value : records) {
            if (value.toLowerCase(Locale.ROOT).contains(normalized)) matches.add(value);
        }
        return Collections.unmodifiableList(matches);
    }

    public synchronized void clearRecords() { records.clear(); }
    public BowChargeConfig getConfig() { return config; }
}
