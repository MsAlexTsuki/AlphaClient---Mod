package com.alpharip.staffmod.staffdashboardexpansion.overloadedstaffalert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Manages staff workload records locally in memory. */
public final class WorkloadManager {
    private final Map<String, StaffWorkload> staffById =
        new LinkedHashMap<String, StaffWorkload>();
    private final WorkloadConfig config;

    public WorkloadManager(WorkloadConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
    }

    public synchronized void registerStaff(String staffId, String staffName) {
        String id = requireText(staffId, "staffId");
        String name = requireText(staffName, "staffName");

        StaffWorkload existing = staffById.get(id);
        if (existing == null) {
            staffById.put(id, new StaffWorkload(id, name));
        } else {
            existing.setStaffName(name);
        }
    }

    public synchronized void setWorkload(String staffId, int workload) {
        getRequiredStaff(staffId).setOpenTasks(workload);
    }

    public synchronized void addTask(String staffId) {
        getRequiredStaff(staffId).addTask();
    }

    public synchronized void completeTask(String staffId) {
        getRequiredStaff(staffId).completeTask();
    }

    public synchronized StaffWorkload getStaffWorkload(String staffId) {
        return getRequiredStaff(staffId).copy();
    }

    public synchronized List<StaffWorkload> getAllStaffWorkloads() {
        List<StaffWorkload> result = new ArrayList<StaffWorkload>();
        for (StaffWorkload workload : staffById.values()) {
            result.add(workload.copy());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<WorkloadAlert> getWarnings() {
        List<WorkloadAlert> result = new ArrayList<WorkloadAlert>();
        for (StaffWorkload staff : staffById.values()) {
            WorkloadLevel level =
                WorkloadLevel.fromWorkload(staff.getOpenTasks(), config);
            if (level != WorkloadLevel.NORMAL) {
                int threshold = level == WorkloadLevel.OVERLOADED
                    ? config.getOverloadedThreshold()
                    : config.getModerateThreshold();

                result.add(new WorkloadAlert(
                    staff.getStaffId(),
                    staff.getStaffName(),
                    staff.getOpenTasks(),
                    threshold,
                    level
                ));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<WorkloadAlert> getAlerts() {
        List<WorkloadAlert> result = new ArrayList<WorkloadAlert>();
        for (WorkloadAlert warning : getWarnings()) {
            if (warning.getLevel() == WorkloadLevel.OVERLOADED) {
                result.add(warning);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private StaffWorkload getRequiredStaff(String staffId) {
        String id = requireText(staffId, "staffId");
        StaffWorkload staff = staffById.get(id);
        if (staff == null) {
            throw new IllegalArgumentException("Unknown staff member: " + id);
        }
        return staff;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
