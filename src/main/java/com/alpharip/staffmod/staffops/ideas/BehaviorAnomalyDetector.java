package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Compares supplied numeric observations with caller-defined expected ranges. */
public class BehaviorAnomalyDetector {
    public static final class Range {
        public final double minimum;
        public final double maximum;

        public Range(double minimum, double maximum) {
            this.minimum = Math.min(minimum, maximum);
            this.maximum = Math.max(minimum, maximum);
        }

        public boolean contains(double value) { return value >= minimum && value <= maximum; }
    }

    private final Map<String, Range> expectedRanges = new LinkedHashMap<String, Range>();

    public void setExpectedRange(String metric, double minimum, double maximum) {
        if (metric != null && !metric.trim().isEmpty()) {
            expectedRanges.put(metric, new Range(minimum, maximum));
        }
    }

    public boolean isOutsideExpectedRange(String metric, double value) {
        Range range = expectedRanges.get(metric);
        return range != null && !range.contains(value);
    }

    public Map<String, Range> getExpectedRanges() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, Range>(expectedRanges));
    }

    public void clear() { expectedRanges.clear(); }
}
