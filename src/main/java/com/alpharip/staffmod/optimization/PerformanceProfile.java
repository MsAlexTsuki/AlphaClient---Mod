// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.optimization;

/**
 * Lightweight sampling presets for Alpha Performance Core.
 * These profiles do not change Minecraft graphics or player options.
 */
public enum PerformanceProfile {
    DEFAULT("Default", 20, 100),
    BALANCED("Balanced", 40, 200),
    PERFORMANCE("Performance", 100, 400),
    MAXIMUM_PERFORMANCE("Maximum Performance", 200, 800);

    private final String displayName;
    private final int monitorIntervalTicks;
    private final int cacheCleanupIntervalTicks;
    private static volatile PerformanceProfile current = BALANCED;

    PerformanceProfile(String displayName, int monitorIntervalTicks, int cacheCleanupIntervalTicks) {
        this.displayName = displayName;
        this.monitorIntervalTicks = monitorIntervalTicks;
        this.cacheCleanupIntervalTicks = cacheCleanupIntervalTicks;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMonitorIntervalTicks() {
        return monitorIntervalTicks;
    }

    public int getCacheCleanupIntervalTicks() {
        return cacheCleanupIntervalTicks;
    }

    public static PerformanceProfile getCurrent() {
        return current;
    }

    public static void setCurrent(PerformanceProfile profile) {
        if (profile != null) {
            current = profile;
        }
    }
}
