package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Summarizes manually supplied workload points and orders staff by workload.
 *
 * Values are only as accurate as the input supplied by the caller.
 * TODO(Supabase): aggregate authorized case/incident workload data from the
 * shared source of truth rather than relying on local values.
 * Compatible with Java 8.
 */
public class WorkloadBalanceBoard {
    public static final class Workload {
        private final String staffId;
        private final int points;

        private Workload(String staffId, int points) {
            this.staffId = staffId;
            this.points = points;
        }

        public String getStaffId() { return staffId; }
        public int getPoints() { return points; }
    }

    private final Map<String, Integer> pointsByStaff = new LinkedHashMap<String, Integer>();

    public void setWorkload(String staffId, int points) {
        String id = requireText(staffId, "staffId");
        if (points < 0) {
            throw new IllegalArgumentException("points cannot be negative.");
        }
        pointsByStaff.put(id, points);
    }

    public void removeStaff(String staffId) {
        pointsByStaff.remove(requireText(staffId, "staffId"));
    }

    public List<Workload> getWorkloads() {
        List<Workload> result = new ArrayList<Workload>();
        for (Map.Entry<String, Integer> entry : pointsByStaff.entrySet()) {
            result.add(new Workload(entry.getKey(), entry.getValue()));
        }
        Collections.sort(result, new Comparator<Workload>() {
            @Override
            public int compare(Workload left, Workload right) {
                int byPoints = Integer.compare(left.getPoints(), right.getPoints());
                return byPoints != 0 ? byPoints : left.getStaffId().compareToIgnoreCase(right.getStaffId());
            }
        });
        return Collections.unmodifiableList(result);
    }

    public double getAverageWorkload() {
        if (pointsByStaff.isEmpty()) {
            return 0.0D;
        }
        long total = 0L;
        for (Integer points : pointsByStaff.values()) {
            total += points;
        }
        return (double) total / (double) pointsByStaff.size();
    }

    public int getWorkloadRange() {
        if (pointsByStaff.isEmpty()) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer points : pointsByStaff.values()) {
            min = Math.min(min, points);
            max = Math.max(max, points);
        }
        return max - min;
    }

    public Map<String, Integer> getSnapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(pointsByStaff));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
