package com.alpharip.staffmod.buildsymmetryguides;

/** Local color palette for the world-space guide overlay. */
public enum SymmetryGuideColor {
    GOLD(0, 0xFFC83D, "Gold"),
    CYAN(1, 0x55DDE0, "Cyan"),
    GREEN(2, 0x75E39A, "Green"),
    PURPLE(3, 0xB58CFF, "Purple");

    private final int id;
    private final int rgb;
    private final String displayName;

    SymmetryGuideColor(int id, int rgb, String displayName) {
        this.id = id; this.rgb = rgb; this.displayName = displayName;
    }
    public int getId() { return id; }
    public int getRgb() { return rgb; }
    public String getDisplayName() { return displayName; }
    public SymmetryGuideColor next() { return values()[(ordinal() + 1) % values().length]; }

    public static SymmetryGuideColor fromId(int id) {
        for (SymmetryGuideColor value : values()) if (value.id == id) return value;
        return GOLD;
    }
}
