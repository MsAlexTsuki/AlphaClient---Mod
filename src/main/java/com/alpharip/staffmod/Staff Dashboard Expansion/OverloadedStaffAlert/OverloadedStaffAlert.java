package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Flags staff whose recorded workload reaches a configurable threshold.
 *
 * Workload inputs must be supplied by a trusted source before operational use.
 * TODO(Supabase): derive workload from authorized active cases/incidents and
 * apply appropriate staff visibility rules.
 * Compatible with Java 8.
 */
public class OverloadedStaffAlert {
    public static final class Alert {
        private final String staffId;
        private final int workload;

        private Alert(String staffId, int workload) {
            this.staffId = staffId;
            this.workload = workload;
        }

        public String getStaffId() { return staffId; }
        public int getWorkload() { return workload; }
    }

    private final Map<String, Integer> workloadByStaff = new LinkedHashMap<String, Integer>();
    private int threshold = 10;

    public void setThreshold(int threshold) {
        if (threshold < 1) {
            throw new IllegalArgumentException("threshold must be at least 1.");
        }
        this.threshold = threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setWorkload(String staffId, int workload) {
        String id = requireText(staffId, "staffId");
        if (workload < 0) {
            throw new IllegalArgumentException("workload cannot be negative.");
        }
        workloadByStaff.put(id, workload);
    }

    public List<Alert> getAlerts() {
        List<Alert> alerts = new ArrayList<Alert>();
        for (Map.Entry<String, Integer> entry : workloadByStaff.entrySet()) {
            if (entry.getValue() >= threshold) {
                alerts.add(new Alert(entry.getKey(), entry.getValue()));
            }
        }
        return Collections.unmodifiableList(alerts);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
