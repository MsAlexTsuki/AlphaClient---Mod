package com.alpharip.staffmod.aimpatternlibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main facade for the Aim Pattern Library prototype.
 * The original numeric observation API is retained for source compatibility.
 */
public final class AimPatternLibrary {
    private final List<Double> legacyValues = new ArrayList<Double>();
    private final AimPatternRegistry registry = new AimPatternRegistry();
    private final AimPatternSettings settings = new AimPatternSettings();
    private final AimPatternService service = new AimPatternService(registry, settings);

    public AimPatternRegistry getRegistry() { return registry; }
    public AimPatternSettings getSettings() { return settings; }
    public AimPatternService getService() { return service; }

    /** Retained compatibility API from the original idea source. */
    public void record(double value) {
        if (isFinite(value)) { legacyValues.add(value); }
    }
    public int size() { return legacyValues.size(); }
    public List<Double> snapshot() {
        return Collections.unmodifiableList(new ArrayList<Double>(legacyValues));
    }
    public double mean() {
        if (legacyValues.isEmpty()) { return 0.0D; }
        double sum = 0.0D;
        for (Double value : legacyValues) { sum += value; }
        return sum / legacyValues.size();
    }
    public void reset() { legacyValues.clear(); }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
