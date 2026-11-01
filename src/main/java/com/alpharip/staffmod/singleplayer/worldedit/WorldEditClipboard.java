// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.state.IBlockState;

/** Stores a copied block region for the current WorldEdit GUI session. */
public final class WorldEditClipboard {
    private IBlockState[][][] blocks;
    private int sizeX, sizeY, sizeZ;

    public void setBlocks(IBlockState[][][] value) {
        if (value == null || value.length == 0 || value[0].length == 0
                || value[0][0].length == 0) {
            clear();
            return;
        }
        blocks = value;
        sizeX = value.length;
        sizeY = value[0].length;
        sizeZ = value[0][0].length;
    }

    public boolean hasBlocks() { return blocks != null; }
    public IBlockState[][][] getBlocks() { return blocks; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }

    public void clear() { blocks = null; sizeX = sizeY = sizeZ = 0; }
}