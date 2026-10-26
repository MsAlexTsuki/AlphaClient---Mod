package com.alpharip.staffmod.staffops.ideas;

/**
 * Client-side presets for safety-related UI and local controls.
 * This is not a security boundary and cannot prevent server-side actions.
 *
 * TODO(Supabase): only add account-scoped profile storage if cross-device
 * synchronization is wanted; keep local defaults usable without a connection.
 */
public final class StaffSafetyProfiles {
    public enum Preset {
        STANDARD, CAUTIOUS, LOCKED_DOWN
    }

    public static final String FEATURE_NAME = "Staff Safety Profiles";
    private Preset preset = Preset.STANDARD;

    public Preset getPreset() {
        return preset;
    }

    public void setPreset(Preset preset) {
        if (preset == null) {
            throw new IllegalArgumentException("Safety preset cannot be null");
        }
        this.preset = preset;
    }

    public boolean requireConfirmationForDestructiveActions() {
        return preset != Preset.STANDARD;
    }

    public boolean allowQuickActions() {
        return preset != Preset.LOCKED_DOWN;
    }
}
