package com.alpharip.staffmod.appealconflictdetector;

/** Immutable reference to a user's involvement in an appeal case. */
public final class AppealInvolvement {
    private final String userId;
    private final AppealInvolvementType type;
    private final String reference;

    public AppealInvolvement(String userId, AppealInvolvementType type, String reference) {
        if (isBlank(userId)) throw new IllegalArgumentException("userId is required");
        if (type == null) throw new IllegalArgumentException("type is required");
        this.userId = userId.trim();
        this.type = type;
        this.reference = reference == null ? "" : reference.trim();
    }

    public String getUserId() { return userId; }
    public AppealInvolvementType getType() { return type; }
    public String getReference() { return reference; }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
