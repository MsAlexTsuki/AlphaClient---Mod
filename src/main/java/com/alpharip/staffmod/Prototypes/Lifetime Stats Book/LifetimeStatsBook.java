package com.alpharip.staffmod;

import java.util.EnumMap;
import java.util.Map;

/** Thread-safe in-memory lifetime counter store. */
public final class LifetimeStatsBook {
    private final Map<LifetimeStat, Long> values = new EnumMap<LifetimeStat, Long>(LifetimeStat.class);

    public LifetimeStatsBook() {
        for (LifetimeStat stat : LifetimeStat.values()) values.put(stat, 0L);
    }

    public synchronized void add(LifetimeStat stat, long amount) {
        if (stat == null) throw new IllegalArgumentException("stat must not be null");
        if (amount < 0L) throw new IllegalArgumentException("amount must not be negative");
        long current = values.get(stat);
        if (Long.MAX_VALUE - current < amount) values.put(stat, Long.MAX_VALUE);
        else values.put(stat, current + amount);
    }

    public synchronized LifetimeStatsSnapshot snapshot() {
        return new LifetimeStatsSnapshot(values);
    }

    public synchronized void reset() {
        for (LifetimeStat stat : LifetimeStat.values()) values.put(stat, 0L);
    }
}
