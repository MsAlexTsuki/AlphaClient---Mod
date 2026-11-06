package com.alpharip.staffmod.staffdashboardexpansion.overloadedstaffalert;

/** Workload classification used by the local staff workload prototype. */
public enum WorkloadLevel {
    NORMAL,
    MODERATE,
    OVERLOADED;

    public static WorkloadLevel fromWorkload(int workload, WorkloadConfig config) {
        if (workload < 0) {
            throw new IllegalArgumentException("workload cannot be negative.");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        if (workload >= config.getOverloadedThreshold()) {
            return OVERLOADED;
        }
        if (workload >= config.getModerateThreshold()) {
            return MODERATE;
        }
        return NORMAL;
    }
}
