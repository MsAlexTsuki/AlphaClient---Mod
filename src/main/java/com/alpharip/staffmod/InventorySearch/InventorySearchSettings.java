package com.alpharip.staffmod.InventorySearch;

/** Client-side on/off setting for InventorySearch. */
public final class InventorySearchSettings {
    private static boolean enabled;
    private InventorySearchSettings() { }
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
}
