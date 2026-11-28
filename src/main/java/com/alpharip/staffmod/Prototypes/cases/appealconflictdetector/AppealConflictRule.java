package com.alpharip.staffmod.appealconflictdetector;

/** A configurable rule declaring an involvement type incompatible with reviewing. */
public final class AppealConflictRule {
    private final AppealInvolvementType involvementType;
    private final String description;
    private final boolean enabled;

    public AppealConflictRule(AppealInvolvementType involvementType, String description, boolean enabled) {
        if (involvementType == null) throw new IllegalArgumentException("involvementType is required");
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("description is required");
        }
        this.involvementType = involvementType;
        this.description = description.trim();
        this.enabled = enabled;
    }

    public AppealInvolvementType getInvolvementType() { return involvementType; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
}
