// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.optimization;

import com.alpharip.staffmod.developer.DeveloperDiagnostics;

/**
 * Combines Alpha Performance Core metrics with existing local developer diagnostics.
 */
public final class PerformanceDiagnostics {
    private PerformanceDiagnostics() {
    }

    public static String createReport() {
        AlphaPerformanceCore core = AlphaPerformanceCore.getInstance();
        PerformanceScheduler scheduler = core.getScheduler();

        StringBuilder report = new StringBuilder("ALPHA PERFORMANCE CORE\n");
        report.append("Initialized: ").append(core.isInitialized()).append("\n");
        report.append("Profile: ").append(core.getProfile().getDisplayName()).append("\n");
        report.append("Scheduler ticks: ").append(scheduler.getCurrentTick()).append("\n");
        report.append("Registered tasks: ").append(scheduler.getTaskCount()).append("\n");
        report.append("Executed tasks: ").append(scheduler.getExecutedTasks()).append("\n");
        report.append("Failed tasks: ").append(scheduler.getFailedTasks()).append("\n");
        report.append("Last scheduled task: ")
                .append(scheduler.getLastTaskDurationNanos() / 1000000.0D).append(" ms\n\n");
        report.append("PERFORMANCE MONITOR\n")
                .append(core.getMonitor().createReport()).append("\n\n");
        report.append("EXISTING DEVELOPER PROFILER\n")
                .append(DeveloperDiagnostics.profilerReport());
        return report.toString();
    }
}
