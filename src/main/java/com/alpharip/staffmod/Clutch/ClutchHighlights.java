package com.alpharip.staffmod.clutch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local clutch-highlight collector. Game-event detection and presentation are not included. */
public class ClutchHighlights {
    public static final String NAME = "Clutch Highlights";
    public static final String CATEGORY = "Clutch";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private final ClutchHighlightsConfig config;
    private final List<ClutchHighlight> highlights = new ArrayList<ClutchHighlight>();
    private final List<String> records = new ArrayList<String>();
    private boolean enabled = true;

    public ClutchHighlights() {
        this(new ClutchHighlightsConfig());
    }

    public ClutchHighlights(ClutchHighlightsConfig config) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");
        this.config = config;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) clearHighlights();
    }

    /** Compatibility API retained from the original idea file. */
    public void addRecord(String record) {
        if (enabled && record != null) {
            records.add(record);
            trimRecords();
        }
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public void clearRecords() { records.clear(); }

    public boolean addHighlight(ClutchHighlightType type, String description, long timestamp) {
        if (!enabled || type == null || description == null || description.trim().isEmpty()) return false;
        highlights.add(new ClutchHighlight(type, description, timestamp));
        trimHighlights();
        return true;
    }

    public List<ClutchHighlight> getHighlights() {
        return Collections.unmodifiableList(new ArrayList<ClutchHighlight>(highlights));
    }

    public ClutchHighlightsConfig getConfig() { return config; }

    public void clearHighlights() { highlights.clear(); }

    public void clearAll() {
        highlights.clear();
        records.clear();
    }

    private void trimHighlights() {
        while (highlights.size() > config.getMaximumHighlights()) highlights.remove(0);
    }

    private void trimRecords() {
        while (records.size() > config.getMaximumHighlights()) records.remove(0);
    }
}
