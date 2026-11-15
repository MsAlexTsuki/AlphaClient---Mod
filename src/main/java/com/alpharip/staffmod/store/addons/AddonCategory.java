package com.alpharip.staffmod.store.addons;

/** Initial catalogue categories; final names can be adjusted when the Store UX is specified. */
public enum AddonCategory {
    ALL("All"),
    HUD("HUD"),
    COSMETIC("Cosmetic"),
    UTILITY("Utility"),
    GAMEPLAY("Gameplay"),
    STAFF("Staff"),
    OTHER("Other");

    private final String displayName;
    AddonCategory(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
