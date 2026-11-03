// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.optimization;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small synchronized TTL cache for reusable client-side performance data.
 * This cache is independent from and does not replace feature-owned caches.
 */
public final class PerformanceCache {
    private static final int MAX_ENTRIES = 512;
    private static final Map<String, CacheEntry> ENTRIES =
            new LinkedHashMap<String, CacheEntry>(64, 0.75F, true);

    private static final class CacheEntry {
        private final Object value;
        private final long expiresAt;

        private CacheEntry(Object value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    private PerformanceCache() {
    }

    public static synchronized void put(String key, Object value, long timeToLiveMillis) {
        if (key == null || key.trim().isEmpty() || value == null || timeToLiveMillis <= 0L) {
            return;
        }
        long now = System.currentTimeMillis();
        ENTRIES.put(key, new CacheEntry(value, now + timeToLiveMillis));
        trimToLimit();
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> T get(String key, Class<T> type) {
        if (key == null || type == null) {
            return null;
        }
        CacheEntry entry = ENTRIES.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt <= System.currentTimeMillis()) {
            ENTRIES.remove(key);
            return null;
        }
        if (!type.isInstance(entry.value)) {
            return null;
        }
        return (T) entry.value;
    }

    public static synchronized boolean contains(String key) {
        return get(key, Object.class) != null;
    }

    public static synchronized void remove(String key) {
        if (key != null) {
            ENTRIES.remove(key);
        }
    }

    public static synchronized void clear() {
        ENTRIES.clear();
    }

    public static synchronized int cleanupExpired() {
        long now = System.currentTimeMillis();
        int removed = 0;
        Iterator<Map.Entry<String, CacheEntry>> iterator = ENTRIES.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiresAt <= now) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public static synchronized int size() {
        cleanupExpired();
        return ENTRIES.size();
    }

    private static void trimToLimit() {
        while (ENTRIES.size() > MAX_ENTRIES) {
            Iterator<String> iterator = ENTRIES.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
            iterator.remove();
        }
    }
}
