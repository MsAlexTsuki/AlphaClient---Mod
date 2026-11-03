// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.optimization;

import com.alpharip.staffmod.developer.DeveloperDiagnostics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs registered lightweight tasks on the Minecraft client thread.
 */
public final class PerformanceScheduler {
    public interface Task {
        void run();
    }

    private static final class ScheduledTask {
        private final String id;
        private final Task task;
        private int intervalTicks;
        private long nextRunTick;

        private ScheduledTask(String id, Task task, int intervalTicks, long nextRunTick) {
            this.id = id;
            this.task = task;
            this.intervalTicks = intervalTicks;
            this.nextRunTick = nextRunTick;
        }
    }

    private final Map<String, ScheduledTask> tasks = new LinkedHashMap<String, ScheduledTask>();
    private long currentTick;
    private long executedTasks;
    private long failedTasks;
    private long lastTaskDurationNanos;

    public synchronized void register(String id, int intervalTicks, Task task) {
        if (id == null || id.trim().isEmpty() || task == null) {
            throw new IllegalArgumentException("A task ID and task are required.");
        }
        int safeInterval = Math.max(1, intervalTicks);
        tasks.put(id, new ScheduledTask(id, task, safeInterval, currentTick + safeInterval));
    }

    public synchronized void updateInterval(String id, int intervalTicks) {
        ScheduledTask scheduled = tasks.get(id);
        if (scheduled == null) {
            return;
        }
        scheduled.intervalTicks = Math.max(1, intervalTicks);
        scheduled.nextRunTick = currentTick + scheduled.intervalTicks;
    }

    public synchronized void unregister(String id) {
        tasks.remove(id);
    }

    public void tick() {
        ScheduledTask[] snapshot;
        long tick;
        synchronized (this) {
            currentTick++;
            tick = currentTick;
            snapshot = tasks.values().toArray(new ScheduledTask[tasks.size()]);
        }

        for (ScheduledTask scheduled : snapshot) {
            synchronized (this) {
                if (tasks.get(scheduled.id) != scheduled || tick < scheduled.nextRunTick) {
                    continue;
                }
                scheduled.nextRunTick = tick + scheduled.intervalTicks;
            }

            long startedAt = System.nanoTime();
            try {
                scheduled.task.run();
                synchronized (this) {
                    executedTasks++;
                }
            } catch (Exception error) {
                synchronized (this) {
                    failedTasks++;
                }
                DeveloperDiagnostics.captureException("Performance task: " + scheduled.id, error);
            } finally {
                synchronized (this) {
                    lastTaskDurationNanos = System.nanoTime() - startedAt;
                }
            }
        }
    }

    public synchronized long getCurrentTick() {
        return currentTick;
    }

    public synchronized int getTaskCount() {
        return tasks.size();
    }

    public synchronized long getExecutedTasks() {
        return executedTasks;
    }

    public synchronized long getFailedTasks() {
        return failedTasks;
    }

    public synchronized long getLastTaskDurationNanos() {
        return lastTaskDurationNanos;
    }
}
