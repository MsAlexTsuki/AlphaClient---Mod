package com.alpharip.staffdashboardexpansion.competency;

/** Skill levels used by the standalone competency matrix prototype. */
public enum StaffCompetencyLevel {
    NOT_ASSESSED("Not assessed", 0),
    BEGINNER("Beginner", 1),
    DEVELOPING("Developing", 2),
    PROFICIENT("Proficient", 3),
    EXPERT("Expert", 4);

    private final String displayName;
    private final int value;

    StaffCompetencyLevel(String displayName, int value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() { return displayName; }
    public int getValue() { return value; }

    public StaffCompetencyLevel next() {
        StaffCompetencyLevel[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
