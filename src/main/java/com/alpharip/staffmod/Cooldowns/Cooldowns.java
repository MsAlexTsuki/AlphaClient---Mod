package com.alpharip.staffmod.cooldowns;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared cooldown registry for Alpha Client modules.
 *
 * This is a lightweight Java foundation only. It deliberately has no
 * Minecraft, Forge, GUI, persistence, or Supabase dependency.
 */
public final class Cooldowns {
    private static final Cooldowns INSTANCE = new Cooldowns();

    private final Map<String, Long> deadlines =
            new LinkedHashMap<String, Long>();

    private Cooldowns() {
    }

    public static Cooldowns getInstance() {
        return INSTANCE;
    }

    /** Starts or refreshes a named cooldown. Duration is expressed in milliseconds. */
    public synchronized void start(String id, long nowMillis, long durationMillis) {
        validateId(id);
        if (durationMillis < 0L) {
            throw new IllegalArgumentException("Cooldown duration cannot be negative.");
        }
        deadlines.put(id, safeAdd(nowMillis, durationMillis));
    }

    public synchronized long getRemainingMillis(String id, long nowMillis) {
        validateId(id);
        Long deadline = deadlines.get(id);
        if (deadline == null) {
            return 0L;
        }
        return Math.max(0L, deadline.longValue() - nowMillis);
    }

    public synchronized boolean isReady(String id, long nowMillis) {
        return getRemainingMillis(id, nowMillis) == 0L;
    }

    public synchronized boolean isActive(String id, long nowMillis) {
        return !isReady(id, nowMillis);
    }

    public synchronized boolean cancel(String id) {
        validateId(id);
        return deadlines.remove(id) != null;
    }

    /** Removes completed entries and returns the number removed. */
    public synchronized int removeExpired(long nowMillis) {
        int removed = 0;
        java.util.Iterator<Map.Entry<String, Long>> iterator =
                deadlines.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().longValue() <= nowMillis) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public synchronized Map<String, Long> getDeadlines() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<String, Long>(deadlines));
    }

    public synchronized void clear() {
        deadlines.clear();
    }

    private static void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Cooldown ID must not be blank.");
        }
    }

    private static long safeAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
