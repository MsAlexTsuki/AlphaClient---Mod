package com.alpharip.staffmod.buildsymmetryguides;

/** Immutable integer block coordinates, independent of Minecraft world objects. */
public final class BlockCoordinates {
    private final int x;
    private final int y;
    private final int z;

    public BlockCoordinates(int x, int y, int z) {
        this.x = x; this.y = y; this.z = z;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BlockCoordinates)) return false;
        BlockCoordinates that = (BlockCoordinates) other;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return 31 * result + z;
    }

    @Override
    public String toString() { return x + ", " + y + ", " + z; }
}
