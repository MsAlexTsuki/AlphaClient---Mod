// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.util.BlockPos;

/** Holds the two selected region corners and calculates normalized bounds. */
public final class WorldEditSelection {
    private BlockPos pos1;
    private BlockPos pos2;

    public void setPos1(BlockPos position) { pos1 = position; }
    public void setPos2(BlockPos position) { pos2 = position; }
    public BlockPos getPos1() { return pos1; }
    public BlockPos getPos2() { return pos2; }
    public boolean hasSelection() { return pos1 != null && pos2 != null; }

    public Bounds getBounds() {
        if (!hasSelection()) return null;
        return new Bounds(Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()),
                Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ()));
    }

    public static final class Bounds {
        public final int minX, minY, minZ, maxX, maxY, maxZ;
        public Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX=minX; this.minY=minY; this.minZ=minZ;
            this.maxX=maxX; this.maxY=maxY; this.maxZ=maxZ;
        }
        public int sizeX() { return maxX-minX+1; }
        public int sizeY() { return maxY-minY+1; }
        public int sizeZ() { return maxZ-minZ+1; }
        public long volume() { return (long) sizeX()*sizeY()*sizeZ(); }
    }
}