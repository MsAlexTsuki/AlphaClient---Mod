package com.alpharip.staffmod.staffdashboardexpansion.overloadedstaffalert;

/** In-memory thresholds for workload classification. */
public final class WorkloadConfig {
    private int moderateThreshold = 5;
    private int overloadedThreshold = 10;

    public int getModerateThreshold() {
        return moderateThreshold;
    }

    public void setModerateThreshold(int moderateThreshold) {
        if (moderateThreshold < 1 || moderateThreshold >= overloadedThreshold) {
            throw new IllegalArgumentException(
                "moderateThreshold must be at least 1 and lower than overloadedThreshold."
            );
        }
        this.moderateThreshold = moderateThreshold;
    }

    public int getOverloadedThreshold() {
        return overloadedThreshold;
    }

    public void setOverloadedThreshold(int overloadedThreshold) {
        if (overloadedThreshold < 1) {
            throw new IllegalArgumentException("overloadedThreshold must be at least 1.");
        }
        this.overloadedThreshold = overloadedThreshold;
        if (moderateThreshold >= overloadedThreshold) {
            this.moderateThreshold = Math.max(1, overloadedThreshold - 1);
        }
    }
}
