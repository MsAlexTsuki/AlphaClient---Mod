package com.alpharip.staffmod.actingrankassignment;

/** Input validation for local acting-rank assignment records. */
public final class ActingRankValidator {
    private ActingRankValidator() { }

    public static String validate(String staffName, String currentRank, String actingRank,
            String assignedBy, String reason, long assignedAtMillis, long expiresAtMillis) {
        if (blank(staffName)) return "Staff name is required.";
        if (staffName.trim().length() > 32) return "Staff name must be 32 characters or fewer.";
        if (blank(currentRank)) return "Current rank is required.";
        if (currentRank.trim().length() > 32) return "Current rank must be 32 characters or fewer.";
        if (blank(actingRank)) return "Acting rank is required.";
        if (actingRank.trim().length() > 32) return "Acting rank must be 32 characters or fewer.";
        if (currentRank.trim().equalsIgnoreCase(actingRank.trim())) return "Acting rank must differ from the current rank.";
        if (blank(assignedBy)) return "Assigning staff member is required.";
        if (assignedBy.trim().length() > 32) return "Assigning staff member must be 32 characters or fewer.";
        if (reason != null && reason.length() > 240) return "Reason must be 240 characters or fewer.";
        if (assignedAtMillis <= 0L) return "Assignment timestamp is invalid.";
        if (expiresAtMillis <= assignedAtMillis) return "Expiry must be later than assignment time.";
        return null;
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
