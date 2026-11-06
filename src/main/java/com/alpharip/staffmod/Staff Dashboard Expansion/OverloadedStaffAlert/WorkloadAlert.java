package com.alpharip.staffmod.staffdashboardexpansion.overloadedstaffalert;

/** Immutable description of a staff workload warning. */
public final class WorkloadAlert {
    private final String staffId;
    private final String staffName;
    private final int openTasks;
    private final int threshold;
    private final WorkloadLevel level;

    public WorkloadAlert(
        String staffId,
        String staffName,
        int openTasks,
        int threshold,
        WorkloadLevel level
    ) {
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new IllegalArgumentException("staffId must not be blank.");
        }
        if (staffName == null || staffName.trim().isEmpty()) {
            throw new IllegalArgumentException("staffName must not be blank.");
        }
        if (openTasks < 0 || threshold < 1) {
            throw new IllegalArgumentException("Invalid workload or threshold.");
        }
        if (level == null) {
            throw new IllegalArgumentException("level must not be null.");
        }

        this.staffId = staffId.trim();
        this.staffName = staffName.trim();
        this.openTasks = openTasks;
        this.threshold = threshold;
        this.level = level;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public int getOpenTasks() {
        return openTasks;
    }

    public int getThreshold() {
        return threshold;
    }

    public WorkloadLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return staffName + " has " + openTasks
            + " open tasks (threshold: " + threshold + ").";
    }
}
