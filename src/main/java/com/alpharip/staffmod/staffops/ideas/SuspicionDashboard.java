package com.alpharip.staffmod.staffops.ideas;
import java.util.LinkedHashMap;
import java.util.Map;
/** Staff telemetry model showing raw measurements only; no automated judgments or accusations. */
public final class SuspicionDashboard {
    public static final class TargetStats {
        public double swingVariance, cpsTrend, averageHitDistance;
        public long sampleCount;
    }
    private final Map<String, TargetStats> targets = new LinkedHashMap<String, TargetStats>();
    public void update(String player, TargetStats stats) { if (player != null && stats != null) targets.put(player, stats); }
    public TargetStats get(String player) { return targets.get(player); }
    public Map<String, TargetStats> getTargets() { return new LinkedHashMap<String, TargetStats>(targets); }
    public void clear() { targets.clear(); }
}
