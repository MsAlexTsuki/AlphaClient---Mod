// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

public class StaffModeManager {

    /*
     * Master Staff Mode
     *
     * Default = OFF
     *
     * This means the staff functionality is disabled
     * until it is explicitly enabled.
     */
    private static boolean masterStaffMode = false;

    /**
     * Returns whether Master Staff Mode is enabled.
     */
    public static boolean isMasterStaffModeEnabled() {
        return masterStaffMode;
    }

    /**
     * Enables or disables Master Staff Mode.
     */
    public static void setMasterStaffMode(boolean enabled) {
        masterStaffMode = enabled;
    }

    /**
     * Toggles Master Staff Mode.
     */
    public static void toggleMasterStaffMode() {
        masterStaffMode = !masterStaffMode;
    }

    /**
     * Resets Master Staff Mode to the safe default.
     */
    public static void reset() {
        masterStaffMode = false;
    }

    /**
     * Loads the Master Staff Mode value from a profile.
     *
     * If the profile does not contain a value,
     * false should be supplied.
     */
    public static void loadFromProfile(boolean enabled) {
        masterStaffMode = enabled;
    }

    /**
     * Returns the current value for saving
     * to the active profile.
     */
    public static boolean getProfileValue() {
        return masterStaffMode;
    }
}