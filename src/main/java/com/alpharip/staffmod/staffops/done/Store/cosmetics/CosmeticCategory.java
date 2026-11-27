package com.alpharip.staffmod.store.cosmetics;

/** Browsing categories for cosmetic catalogue entries. */
public enum CosmeticCategory {
    ALL("All"),
    CAPES("Capes"),
    TRAILS("Trails"),
    EMOTES("Emotes"),
    NAMES("Names"),
    CHAT("Chat"),
    EFFECTS("Effects"),
    UI("UI"),
    OTHER("Other");

    private final String displayName;
    CosmeticCategory(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
