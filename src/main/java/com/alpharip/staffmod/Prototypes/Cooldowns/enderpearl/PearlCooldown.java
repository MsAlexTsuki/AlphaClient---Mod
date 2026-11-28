package com.alpharip.staffmod.cooldowns.enderpearl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ender Pearl cooldown module foundation.
 *
 * This module owns pearl-specific cooldown settings and records. Runtime
 * detection and HUD integration can be connected in a later implementation.
 */
public final class PearlCooldown {
    public static final String FEATURE_NAME = "Pearl Cooldown";
    public static final String DESCRIPTION =
            "Tracks ender-pearl cooldown deadlines and remaining time.";

    private boolean enabled = true;
    private long cooldownDurationMillis = 15000L;
    private long deadlineMillis;
    private final Map<String, String> options = new LinkedHashMap<String, String>();
    private final List<String> records = new ArrayList<String>();

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean value) {
        enabled = value;
    }

    public synchronized long getCooldownDurationMillis() {
        return cooldownDurationMillis;
    }

    public synchronized void setCooldownDurationMillis(long durationMillis) {
        if (durationMillis < 0L) {
            throw new IllegalArgumentException("Cooldown duration cannot be negative.");
        }
        cooldownDurationMillis = durationMillis;
    }

    /** Starts or refreshes the pearl cooldown using the configured duration. */
    public synchronized void start(long nowMillis) {
        deadlineMillis = safeAdd(nowMillis, cooldownDurationMillis);
    }

    /** Starts or refreshes the cooldown with an explicit duration. */
    public synchronized void start(long nowMillis, long durationMillis) {
        if (durationMillis < 0L) {
            throw new IllegalArgumentException("Cooldown duration cannot be negative.");
        }
        deadlineMillis = safeAdd(nowMillis, durationMillis);
    }

    public synchronized long remaining(long nowMillis) {
        return Math.max(0L, deadlineMillis - nowMillis);
    }

    public synchronized boolean isReady(long nowMillis) {
        return remaining(nowMillis) == 0L;
    }

    public synchronized void reset() {
        deadlineMillis = 0L;
    }

    public synchronized void setOption(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }
        if (value == null) {
            options.remove(key);
        } else {
            options.put(key, value);
        }
    }

    public synchronized String getOption(String key, String fallback) {
        String value = options.get(key);
        return value == null ? fallback : value;
    }

    public synchronized Map<String, String> getOptions() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<String, String>(options));
    }

    public synchronized void record(String value) {
        if (value != null) {
            records.add(value);
        }
    }

    public synchronized List<String> getRecords() {
        return Collections.unmodifiableList(
                new ArrayList<String>(records));
    }

    public synchronized List<String> searchRecords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getRecords();
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<String>();
        for (String value : records) {
            if (value.toLowerCase(Locale.ROOT).contains(normalized)) {
                matches.add(value);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    public synchronized void clearRecords() {
        records.clear();
    }

    private static long safeAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
