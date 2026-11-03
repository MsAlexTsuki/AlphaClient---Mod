// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.optimization;

/**
 * Central lifecycle and coordination point for Alpha Client performance services.
 * All scheduled work is executed from the client tick thread.
 */
public final class AlphaPerformanceCore {
    private static final AlphaPerformanceCore INSTANCE = new AlphaPerformanceCore();

    private final PerformanceScheduler scheduler = new PerformanceScheduler();
    private final PerformanceMonitor monitor = new PerformanceMonitor();
    private boolean initialized;

    private AlphaPerformanceCore() {
    }

    public static AlphaPerformanceCore getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize() {
        if (initialized) {
            return;
        }

        scheduler.register("performance-monitor",
                PerformanceProfile.getCurrent().getMonitorIntervalTicks(),
                new PerformanceScheduler.Task() {
                    @Override
                    public void run() {
                        monitor.sample();
                    }
                });

        scheduler.register("performance-cache-cleanup",
                PerformanceProfile.getCurrent().getCacheCleanupIntervalTicks(),
                new PerformanceScheduler.Task() {
                    @Override
                    public void run() {
                        PerformanceCache.cleanupExpired();
                    }
                });

        initialized = true;
    }

    public void onClientTick() {
        if (!initialized) {
            initialize();
        }
        long startedAt = System.nanoTime();
        scheduler.tick();
        monitor.recordCoreUpdateDuration(System.nanoTime() - startedAt);
    }

    public synchronized void setProfile(PerformanceProfile profile) {
        if (profile == null) {
            return;
        }
        PerformanceProfile.setCurrent(profile);
        if (initialized) {
            scheduler.updateInterval("performance-monitor", profile.getMonitorIntervalTicks());
            scheduler.updateInterval("performance-cache-cleanup", profile.getCacheCleanupIntervalTicks());
        }
    }

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public PerformanceScheduler getScheduler() {
        return scheduler;
    }

    public PerformanceMonitor getMonitor() {
        return monitor;
    }

    public PerformanceProfile getProfile() {
        return PerformanceProfile.getCurrent();
    }
}
