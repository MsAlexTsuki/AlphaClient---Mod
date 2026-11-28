package com.alpharip.staffdashboardexpansion.competency;

/** Local assessment of one staff member for one defined competency. */
public final class StaffCompetencyAssessment {
    private final String staffName;
    private final String competencyId;
    private StaffCompetencyLevel level;
    private String notes;
    private long assessedAt;

    StaffCompetencyAssessment(String staffName, String competencyId,
                              StaffCompetencyLevel level, String notes) {
        this.staffName = staffName;
        this.competencyId = competencyId;
        this.level = level;
        this.notes = notes;
        this.assessedAt = System.currentTimeMillis();
    }

    public String getStaffName() { return staffName; }
    public String getCompetencyId() { return competencyId; }
    public StaffCompetencyLevel getLevel() { return level; }
    public String getNotes() { return notes; }
    public long getAssessedAt() { return assessedAt; }

    void update(StaffCompetencyLevel level, String notes) {
        this.level = level;
        this.notes = notes;
        this.assessedAt = System.currentTimeMillis();
    }
}
