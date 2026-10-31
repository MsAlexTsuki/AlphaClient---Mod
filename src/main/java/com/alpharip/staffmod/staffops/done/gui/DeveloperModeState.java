package com.alpharip.staffmod.gui;

/** Session-only toggle for the restricted Developer Mode workspace. */
public final class DeveloperModeState {
    private static volatile boolean enabled;

    private DeveloperModeState() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void disable() {
        enabled = false;
    }
}
