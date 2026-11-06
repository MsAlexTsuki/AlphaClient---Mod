package com.alpharip.staffmod.aimpatternlibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Bounded in-memory registry; it does not persist or synchronize patterns. */
public final class AimPatternRegistry {
    private final Map<String, AimPattern> patterns = new LinkedHashMap<String, AimPattern>();
    private int maximumPatterns = 50;

    public synchronized AimPattern create(String name, AimPatternCategory category) {
        String validName = AimPatternValidator.validateName(name);
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        if (patterns.size() >= maximumPatterns) throw new IllegalStateException("Pattern limit reached");
        AimPattern pattern = new AimPattern(UUID.randomUUID().toString(), validName, category,
                System.currentTimeMillis(), Collections.<Double>emptyList());
        patterns.put(pattern.getId(), pattern);
        return pattern;
    }
    public synchronized boolean addObservation(String id, double value) {
        AimPatternValidator.validateObservation(value);
        AimPattern current = patterns.get(id);
        if (current == null) return false;
        List<Double> values = new ArrayList<Double>(current.getObservations());
        values.add(value);
        patterns.put(id, new AimPattern(current.getId(), current.getName(), current.getCategory(),
                current.getCreatedAt(), values));
        return true;
    }
    public synchronized boolean rename(String id, String name) {
        AimPattern current = patterns.get(id);
        if (current == null) return false;
        patterns.put(id, new AimPattern(id, AimPatternValidator.validateName(name), current.getCategory(),
                current.getCreatedAt(), current.getObservations()));
        return true;
    }
    public synchronized boolean setCategory(String id, AimPatternCategory category) {
        AimPattern current = patterns.get(id);
        if (current == null) return false;
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        patterns.put(id, new AimPattern(id, current.getName(), category, current.getCreatedAt(),
                current.getObservations()));
        return true;
    }
    public synchronized boolean remove(String id) { return patterns.remove(id) != null; }
    public synchronized AimPattern get(String id) { return patterns.get(id); }
    public synchronized List<AimPattern> snapshot() {
        return Collections.unmodifiableList(new ArrayList<AimPattern>(patterns.values()));
    }
    public synchronized int size() { return patterns.size(); }
    public synchronized void clear() { patterns.clear(); }
    public synchronized void setMaximumPatterns(int maximum) {
        if (maximum < 1 || maximum > 500) throw new IllegalArgumentException("Maximum must be 1-500");
        if (patterns.size() > maximum) throw new IllegalStateException("Remove patterns before lowering the limit");
        maximumPatterns = maximum;
    }
    public synchronized int getMaximumPatterns() { return maximumPatterns; }
}
