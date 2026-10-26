package com.alpharip.staffmod.staffops.ideas;

/**
 * Client-side foundation for configurable confirmation prompts.
 *
 * Scope: local prompt policy only; this class does not execute staff actions.
 * TODO(Supabase): if profiles must follow an account between devices, define a
 * profile table and account-scoped read/write policy before adding persistence.
 */
public final class StaffConfirmationProfiles {
    public enum ConfirmationLevel {
        NEVER, DESTRUCTIVE_ACTIONS, ALL_ACTIONS
    }

    public static final String FEATURE_NAME = "Staff Confirmation Profiles";
    private ConfirmationLevel level = ConfirmationLevel.DESTRUCTIVE_ACTIONS;

    public ConfirmationLevel getLevel() {
        return level;
    }

    public void setLevel(ConfirmationLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("Confirmation level cannot be null");
        }
        this.level = level;
    }

    public boolean shouldConfirm(boolean destructiveAction) {
        switch (level) {
            case NEVER:
                return false;
            case ALL_ACTIONS:
                return true;
            case DESTRUCTIVE_ACTIONS:
                return destructiveAction;
            default:
                return true;
        }
    }
}
