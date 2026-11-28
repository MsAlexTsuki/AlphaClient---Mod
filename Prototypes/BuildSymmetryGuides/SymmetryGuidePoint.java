package com.alpharip.staffmod.buildsymmetryguides;

/** One calculated destination block and the reflection that produced it. */
public final class SymmetryGuidePoint {
    private final BlockCoordinates position;
    private final String reflection;

    public SymmetryGuidePoint(BlockCoordinates position, String reflection) {
        if (position == null) throw new IllegalArgumentException("position");
        this.position = position;
        this.reflection = reflection == null ? "" : reflection;
    }
    public BlockCoordinates getPosition() { return position; }
    public String getReflection() { return reflection; }
}
