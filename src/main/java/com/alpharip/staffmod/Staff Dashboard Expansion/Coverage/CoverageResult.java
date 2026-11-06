package com.alpharip.staffdashboardexpansion.coverage;

/** Immutable result for one coverage requirement. */
public final class CoverageResult {
    private final CoverageRequirement requirement;
    private final int scheduledStaff;
    private final int shortage;
    public CoverageResult(CoverageRequirement requirement, int scheduledStaff) {
        if (requirement == null) throw new IllegalArgumentException("requirement cannot be null.");
        if (scheduledStaff < 0) throw new IllegalArgumentException("scheduledStaff cannot be negative.");
        this.requirement = requirement;
        this.scheduledStaff = scheduledStaff;
        this.shortage = Math.max(0, requirement.getMinimumStaff() - scheduledStaff);
    }
    public CoverageRequirement getRequirement() { return requirement; }
    public int getScheduledStaff() { return scheduledStaff; }
    public int getMinimumStaff() { return requirement.getMinimumStaff(); }
    public int getShortage() { return shortage; }
    public boolean isUnderstaffed() { return shortage > 0; }
    public boolean isCovered() { return shortage == 0; }
}