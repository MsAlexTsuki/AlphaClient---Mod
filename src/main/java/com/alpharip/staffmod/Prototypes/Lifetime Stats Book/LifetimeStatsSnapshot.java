package com.alpharip.staffmod;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Immutable point-in-time view of lifetime counters. */
public final class LifetimeStatsSnapshot {
    private final Map<LifetimeStat, Long> values;

    LifetimeStatsSnapshot(Map<LifetimeStat, Long> source) {
        this.values = Collections.unmodifiableMap(new EnumMap<LifetimeStat, Long>(source));
    }

    public long get(LifetimeStat stat) {
        Long value = values.get(stat);
        return value == null ? 0L : value;
    }

    public Map<LifetimeStat, Long> getAll() { return values; }
}
