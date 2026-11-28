package com.alpharip.staffmod.Singleplayer.EasyCraft;

/** Simple recipe-book categories used by the Easy Craft screen. */
public enum EasyCraftCategory {
    ALL("All"),
    BUILDING("Building"),
    TOOLS("Tools"),
    COMBAT("Combat"),
    FOOD("Food"),
    REDSTONE("Redstone"),
    MISC("Misc");

    private final String displayName;

    EasyCraftCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
