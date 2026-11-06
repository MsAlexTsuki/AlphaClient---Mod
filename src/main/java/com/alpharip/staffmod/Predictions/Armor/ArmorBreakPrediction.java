package com.alpharip.staffmod.predictions.armor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Local armor-break prediction prototype.
 * It estimates break timing from observed durability loss supplied by a future
 * Minecraft combat-event integration; it does not inspect entities or items itself.
 */
public final class ArmorBreakPrediction {
    public static final String FEATURE_NAME = "Armor Break Prediction";
    public static final String DESCRIPTION =
            "Estimates remaining opponent armor durability and hits until an armor piece breaks.";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    /** Kept public for compatibility with the original idea file. */
    public boolean enabled = true;
    /** "all" tracks every opponent; another value filters to that opponent name. */
    public String filter = "all";

    private final ArmorBreakPredictionConfig config;
    private final Map<String, Map<ArmorSlot, ArmorDurabilityTracker>> opponents =
            new LinkedHashMap<String, Map<ArmorSlot, ArmorDurabilityTracker>>();

    public ArmorBreakPrediction() {
        this(ArmorBreakPredictionConfig.defaults());
    }

    public ArmorBreakPrediction(ArmorBreakPredictionConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        this.config = config;
    }

    public ArmorBreakPredictionConfig getConfig() { return config; }

    /**
     * Records observed durability loss for a specific opponent armor slot.
     * durabilityDamage must be the actual item-durability points lost, not raw hit damage.
     */
    public void recordArmorDamage(String opponentName, ArmorSlot slot, int durabilityDamage,
                                  int maximumDurability) {
        if (!enabled || opponentName == null || opponentName.trim().isEmpty()
                || slot == null || durabilityDamage <= 0 || maximumDurability <= 0
                || !matchesFilter(opponentName)) {
            return;
        }

        Map<ArmorSlot, ArmorDurabilityTracker> armor = opponents.get(opponentName);
        if (armor == null) {
            armor = new LinkedHashMap<ArmorSlot, ArmorDurabilityTracker>();
            opponents.put(opponentName, armor);
        }

        ArmorDurabilityTracker tracker = armor.get(slot);
        if (tracker == null || trackerMaximumChanged(tracker, maximumDurability)) {
            tracker = new ArmorDurabilityTracker(opponentName, slot, maximumDurability,
                    config.getMaxSamplesPerSlot());
            armor.put(slot, tracker);
        }
        tracker.recordDamage(durabilityDamage);
    }

    public ArmorBreakResult getPrediction(String opponentName, ArmorSlot slot) {
        if (opponentName == null || slot == null) return null;
        Map<ArmorSlot, ArmorDurabilityTracker> armor = opponents.get(opponentName);
        if (armor == null) return null;
        ArmorDurabilityTracker tracker = armor.get(slot);
        return tracker == null ? null : tracker.createResult(config.getMinimumSamples());
    }

    public Map<ArmorSlot, ArmorBreakResult> getPredictions(String opponentName) {
        Map<ArmorSlot, ArmorBreakResult> results = new LinkedHashMap<ArmorSlot, ArmorBreakResult>();
        Map<ArmorSlot, ArmorDurabilityTracker> armor = opponents.get(opponentName);
        if (armor != null) {
            for (Map.Entry<ArmorSlot, ArmorDurabilityTracker> entry : armor.entrySet()) {
                results.put(entry.getKey(), entry.getValue().createResult(config.getMinimumSamples()));
            }
        }
        return Collections.unmodifiableMap(results);
    }

    public void clearOpponent(String opponentName) {
        if (opponentName != null) opponents.remove(opponentName);
    }

    public void clear() {
        opponents.clear();
    }

    private boolean matchesFilter(String opponentName) {
        return filter == null || filter.trim().isEmpty() || "all".equalsIgnoreCase(filter.trim())
                || opponentName.equalsIgnoreCase(filter.trim());
    }

    private boolean trackerMaximumChanged(ArmorDurabilityTracker tracker, int maximumDurability) {
        ArmorBreakResult current = tracker.createResult(1);
        return current.getMaximumDurability() != maximumDurability;
    }
}
