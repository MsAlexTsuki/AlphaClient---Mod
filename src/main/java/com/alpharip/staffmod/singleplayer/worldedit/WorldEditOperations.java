// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

/** Contains the server-thread block operations used by the WorldEdit GUI. */
public final class WorldEditOperations {
    private WorldEditOperations() { }

    public static boolean regionLoaded(WorldServer world, WorldEditSelection.Bounds bounds) {
        for (int x = bounds.minX >> 4; x <= bounds.maxX >> 4; x++) {
            for (int z = bounds.minZ >> 4; z <= bounds.maxZ >> 4; z++) {
                if (!world.getChunkProvider().chunkExists(x, z)) return false;
            }
        }
        return true;
    }

    public static void fill(WorldServer world, WorldEditSelection.Bounds bounds,
                            IBlockState state, IBlockState filter) {
        for (int x=bounds.minX; x<=bounds.maxX; x++)
            for (int y=bounds.minY; y<=bounds.maxY; y++)
                for (int z=bounds.minZ; z<=bounds.maxZ; z++) {
                    BlockPos pos = new BlockPos(x,y,z);
                    if (filter == null || world.getBlockState(pos).getBlock() == filter.getBlock())
                        world.setBlockState(pos, state, 2);
                }
    }

    public static void shape(WorldServer world, WorldEditSelection.Bounds bounds,
                             IBlockState state, int operation,
                             int wallsId, int outlineId, int hollowId) {
        for (int x=bounds.minX; x<=bounds.maxX; x++)
            for (int y=bounds.minY; y<=bounds.maxY; y++)
                for (int z=bounds.minZ; z<=bounds.maxZ; z++) {
                    boolean xEdge = x==bounds.minX || x==bounds.maxX;
                    boolean yEdge = y==bounds.minY || y==bounds.maxY;
                    boolean zEdge = z==bounds.minZ || z==bounds.maxZ;
                    boolean edge = operation == wallsId ? xEdge || zEdge : xEdge || yEdge || zEdge;
                    if (!edge && operation == hollowId) {
                        world.setBlockState(new BlockPos(x,y,z), Blocks.air.getDefaultState(), 2);
                    } else if (edge) {
                        world.setBlockState(new BlockPos(x,y,z), state, 2);
                    }
                }
    }

    public static IBlockState[][][] copy(WorldServer world, WorldEditSelection.Bounds bounds) {
        int sx=bounds.sizeX(), sy=bounds.sizeY(), sz=bounds.sizeZ();
        IBlockState[][][] result = new IBlockState[sx][sy][sz];
        for (int x=0; x<sx; x++)
            for (int y=0; y<sy; y++)
                for (int z=0; z<sz; z++)
                    result[x][y][z] = world.getBlockState(
                            new BlockPos(bounds.minX+x,bounds.minY+y,bounds.minZ+z));
        return result;
    }

    public static void paste(WorldServer world, BlockPos destination, IBlockState[][][] blocks) {
        for (int x=0; x<blocks.length; x++)
            for (int y=0; y<blocks[x].length; y++)
                for (int z=0; z<blocks[x][y].length; z++)
                    world.setBlockState(new BlockPos(destination.getX()+x,
                            destination.getY()+y,destination.getZ()+z), blocks[x][y][z], 2);
    }
}