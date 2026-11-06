package com.alpharip.staffmod.staffdashboardexpansion.overloadedstaffalert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local prototype for detecting overloaded staff members.
 *
 * All data is kept in memory. This class has no network, database,
 * Supabase, or persistence dependencies.
 *
 * Compatible with Java 8.
 */
public final class OverloadedStaffAlert {
    public static final class Alert {
        private final String staffId;
        private final String staffName;
        private final int workload;

        private Alert(String staffId, String staffName, int workload) {
            this.staffId = staffId;
            this.staffName = staffName;
            this.workload = workload;
        }

        public String getStaffId() {
            return staffId;
        }

        public String getStaffName() {
            return staffName;
        }

        public int getWorkload() {
            return workload;
        }
    }

    private final WorkloadConfig config;
    private final WorkloadManager manager;

    public OverloadedStaffAlert() {
        this(new WorkloadConfig());
    }

    public OverloadedStaffAlert(WorkloadConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
        this.manager = new WorkloadManager(config);
    }

    /**
     * Keeps the original threshold API. The default overload threshold is 10.
     */
    public void setThreshold(int threshold) {
        config.setOverloadedThreshold(threshold);
    }

    public int getThreshold() {
        return config.getOverloadedThreshold();
    }

    public void registerStaff(String staffId, String staffName) {
        manager.registerStaff(staffId, staffName);
    }

    public void setWorkload(String staffId, int workload) {
        manager.setWorkload(staffId, workload);
    }

    public void addTask(String staffId) {
        manager.addTask(staffId);
    }

    public void completeTask(String staffId) {
        manager.completeTask(staffId);
    }

    /**
     * Returns only staff members at or above the overload threshold.
     */
    public List<Alert> getAlerts() {
        List<Alert> alerts = new ArrayList<Alert>();
        for (WorkloadAlert alert : manager.getAlerts()) {
            if (alert.getLevel() == WorkloadLevel.OVERLOADED) {
                alerts.add(new Alert(
                    alert.getStaffId(),
                    alert.getStaffName(),
                    alert.getOpenTasks()
                ));
            }
        }
        return Collections.unmodifiableList(alerts);
    }

    /**
     * Returns all staff workload warnings, including the moderate level.
     */
    public List<WorkloadAlert> getWarnings() {
        return manager.getWarnings();
    }

    public StaffWorkload getStaffWorkload(String staffId) {
        return manager.getStaffWorkload(staffId);
    }

    public List<StaffWorkload> getAllStaffWorkloads() {
        return manager.getAllStaffWorkloads();
    }

    public WorkloadConfig getConfig() {
        return config;
    }
}
