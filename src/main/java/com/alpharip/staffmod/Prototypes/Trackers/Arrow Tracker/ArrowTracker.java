package com.alpharip.staffmod.trackers.arrow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tracks known arrows and their latest positions.
 * Client-side prototype only; Forge entity hooks and rendering are not included.
 */
public final class ArrowTracker {
    public static final String FEATURE_NAME = "Arrow Tracker";
    public static final String DESCRIPTION = "Tracks arrows, their positions, distances and flight times.";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private boolean enabled = true;
    private final ArrowTrackerConfig config;
    private final Map<String, ArrowTrack> arrows = new LinkedHashMap<String, ArrowTrack>();
    private final List<String> records = new ArrayList<String>();

    public ArrowTracker() {
        this(new ArrowTrackerConfig());
    }

    public ArrowTracker(ArrowTrackerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        this.config = config;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            arrows.clear();
        }
    }

    public ArrowTrackerConfig getConfig() {
        return config;
    }

    public boolean trackArrow(String id, double x, double y, double z, long timestamp) {
        if (!enabled || id == null || id.trim().isEmpty()
                || !isFinite(x) || !isFinite(y) || !isFinite(z) || timestamp < 0L) {
            return false;
        }

        ArrowTrack existing = arrows.get(id);
        if (existing == null) {
            if (arrows.size() >= config.getMaximumTrackedArrows()) {
                return false;
            }
            arrows.put(id, new ArrowTrack(id, x, y, z, timestamp));
        } else {
            existing.updatePosition(x, y, z, timestamp);
        }
        return true;
    }

    public boolean updateArrow(String id, double x, double y, double z, long timestamp) {
        ArrowTrack track = arrows.get(id);
        if (!enabled || track == null || !isFinite(x) || !isFinite(y)
                || !isFinite(z) || timestamp < track.getLastUpdatedAt()) {
            return false;
        }
        track.updatePosition(x, y, z, timestamp);
        return true;
    }

    public boolean removeArrow(String id) {
        return arrows.remove(id) != null;
    }

    public List<ArrowTrackerSnapshot> getTrackedArrows() {
        List<ArrowTrackerSnapshot> result = new ArrayList<ArrowTrackerSnapshot>();
        for (ArrowTrack track : arrows.values()) {
            result.add(track.snapshot());
        }
        return Collections.unmodifiableList(result);
    }

    public ArrowTrackerSnapshot getArrow(String id) {
        ArrowTrack track = arrows.get(id);
        return track == null ? null : track.snapshot();
    }

    public void record(String value) {
        if (value != null) {
            records.add(value);
        }
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public List<String> searchRecords(String query) {
        List<String> result = new ArrayList<String>();
        if (query == null || query.trim().isEmpty()) {
            return getRecords();
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        for (String record : records) {
            if (record.toLowerCase(Locale.ROOT).contains(normalized)) {
                result.add(record);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void clearRecords() {
        records.clear();
    }

    public void clearTrackedArrows() {
        arrows.clear();
    }

    public void clear() {
        arrows.clear();
        records.clear();
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
