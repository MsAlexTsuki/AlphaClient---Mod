package com.alpharip.staffmod.aimpatternlibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable snapshot of one named aim-observation pattern. */
public final class AimPattern {
    private final String id;
    private final String name;
    private final AimPatternCategory category;
    private final long createdAt;
    private final List<Double> observations;

    public AimPattern(String id, String name, AimPatternCategory category, long createdAt, List<Double> observations) {
        if (id == null || name == null || category == null || observations == null) {
            throw new IllegalArgumentException("Pattern fields cannot be null");
        }
        this.id = id;
        this.name = name;
        this.category = category;
        this.createdAt = createdAt;
        List<Double> copy = new ArrayList<Double>();
        for (Double value : observations) {
            if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) copy.add(value);
        }
        this.observations = Collections.unmodifiableList(copy);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public AimPatternCategory getCategory() { return category; }
    public long getCreatedAt() { return createdAt; }
    public List<Double> getObservations() { return observations; }
    public int getObservationCount() { return observations.size(); }
}
