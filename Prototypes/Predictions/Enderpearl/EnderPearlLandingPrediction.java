package com.alpharip.staffmod.predictions.enderpearl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ender Pearl Landing Prediction prototype.
 * Simulates a local projectile arc; Minecraft entity hooks and world collision probing
 * must be supplied by a later runtime integration.
 */
public final class EnderPearlLandingPrediction {
    public static final String FEATURE_NAME = "Ender Pearl Landing Prediction";
    public static final String DESCRIPTION = "Simulates an Ender Pearl trajectory and estimates its landing position.";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private boolean enabled = true;
    private final EnderPearlPredictionConfig config;
    private final EnderPearlTrajectoryPredictor predictor;
    private final Map<String, String> options = new LinkedHashMap<String, String>();
    private final List<String> records = new ArrayList<String>();

    public EnderPearlLandingPrediction() {
        this(EnderPearlPredictionConfig.defaults());
    }

    public EnderPearlLandingPrediction(EnderPearlPredictionConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        this.config = config;
        this.predictor = new EnderPearlTrajectoryPredictor(config);
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public EnderPearlPredictionConfig getConfig() { return config; }

    public EnderPearlPredictionResult predict(EnderPearlVector position,
                                               EnderPearlVector motion,
                                               EnderPearlTrajectoryPredictor.CollisionProbe collisionProbe) {
        if (!enabled) return null;
        return predictor.predict(position, motion, collisionProbe);
    }

    public void setOption(String key, String value) {
        if (key == null || key.trim().isEmpty()) return;
        if (value == null) options.remove(key);
        else options.put(key, value);
    }

    public String getOption(String key, String fallback) {
        String value = options.get(key);
        return value == null ? fallback : value;
    }

    public Map<String, String> getOptions() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(options));
    }

    public void record(String value) {
        if (value != null) records.add(value);
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public List<String> searchRecords(String query) {
        List<String> matches = new ArrayList<String>();
        if (query == null || query.trim().isEmpty()) return getRecords();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        for (String value : records) {
            if (value.toLowerCase(Locale.ROOT).contains(normalizedQuery)) matches.add(value);
        }
        return matches;
    }

    public void clearRecords() {
        records.clear();
    }
}
