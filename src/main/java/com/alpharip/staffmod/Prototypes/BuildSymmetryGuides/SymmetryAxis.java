package com.alpharip.staffmod.buildsymmetryguides;

/** Selects the vertical mirror plane or planes used by the calculator. */
public enum SymmetryAxis {
    X(0, "X plane", true, false),
    Z(1, "Z plane", false, true),
    BOTH(2, "X + Z planes", true, true);

    private final int id;
    private final String displayName;
    private final boolean reflectX;
    private final boolean reflectZ;

    SymmetryAxis(int id, String displayName, boolean reflectX, boolean reflectZ) {
        this.id = id;
        this.displayName = displayName;
        this.reflectX = reflectX;
        this.reflectZ = reflectZ;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public boolean reflectsX() { return reflectX; }
    public boolean reflectsZ() { return reflectZ; }
    public SymmetryAxis next() { return values()[(ordinal() + 1) % values().length]; }

    public static SymmetryAxis fromId(int id) {
        for (SymmetryAxis value : values()) if (value.id == id) return value;
        return X;
    }
}
