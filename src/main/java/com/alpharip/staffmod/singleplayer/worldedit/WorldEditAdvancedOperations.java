// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

/** Independent region and primitive-generation operations for Alpha World Edit. */
public final class WorldEditAdvancedOperations {
    private WorldEditAdvancedOperations() { }

    public static void line(WorldServer w, BlockPos a, BlockPos b, IBlockState state) {
        int dx=b.getX()-a.getX(), dy=b.getY()-a.getY(), dz=b.getZ()-a.getZ();
        int steps=Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) { w.setBlockState(a, state, 2); return; }
        for (int i=0;i<=steps;i++) {
            double t=(double)i/(double)steps;
            w.setBlockState(new BlockPos(Math.round(a.getX()+dx*t),
                    Math.round(a.getY()+dy*t), Math.round(a.getZ()+dz*t)), state, 2);
        }
    }

    public static void center(WorldServer w, WorldEditSelection.Bounds b, IBlockState state) {
        w.setBlockState(new BlockPos((b.minX+b.maxX)/2, (b.minY+b.maxY)/2,
                (b.minZ+b.maxZ)/2), state, 2);
    }

    public static void overlay(WorldServer w, WorldEditSelection.Bounds b, IBlockState state) {
        for (int x=b.minX;x<=b.maxX;x++) for (int z=b.minZ;z<=b.maxZ;z++) {
            BlockPos top=w.getTopSolidOrLiquidBlock(new BlockPos(x,b.maxY,z));
            if (top.getY() >= b.minY && top.getY() <= b.maxY)
                w.setBlockState(top.up(), state, 2);
        }
    }

    public static void naturalize(WorldServer w, WorldEditSelection.Bounds b) {
        for (int x=b.minX;x<=b.maxX;x++) for (int z=b.minZ;z<=b.maxZ;z++) {
            int surface=-1;
            for (int y=b.maxY;y>=b.minY;y--) {
                if (!w.isAirBlock(new BlockPos(x,y,z))) { surface=y; break; }
            }
            if (surface < 0) continue;
            w.setBlockState(new BlockPos(x,surface,z), Blocks.grass.getDefaultState(), 2);
            if (surface-1 >= b.minY) w.setBlockState(new BlockPos(x,surface-1,z), Blocks.dirt.getDefaultState(), 2);
            if (surface-2 >= b.minY) w.setBlockState(new BlockPos(x,surface-2,z), Blocks.dirt.getDefaultState(), 2);
        }
    }

    public static void move(WorldServer w, WorldEditSelection.Bounds b, int dx, int dy, int dz) {
        IBlockState[][][] copy=WorldEditOperations.copy(w,b);
        WorldEditOperations.fill(w,b,Blocks.air.getDefaultState(),null);
        WorldEditOperations.paste(w,new BlockPos(b.minX+dx,b.minY+dy,b.minZ+dz),copy);
    }

    public static void stack(WorldServer w, WorldEditSelection.Bounds b, int count, int dx, int dy, int dz) {
        IBlockState[][][] copy=WorldEditOperations.copy(w,b);
        for(int n=1;n<=count;n++)
            WorldEditOperations.paste(w,new BlockPos(b.minX+dx*n,b.minY+dy*n,b.minZ+dz*n),copy);
    }

    public static void sphere(WorldServer w, BlockPos c, int rx, int ry, int rz,
                              IBlockState state, boolean hollow) {
        for(int x=-rx;x<=rx;x++) for(int y=-ry;y<=ry;y++) for(int z=-rz;z<=rz;z++) {
            double d=(double)x*x/(rx*rx)+(double)y*y/(ry*ry)+(double)z*z/(rz*rz);
            if(d<=1.0 && (!hollow || d>=0.75))
                w.setBlockState(c.add(x,y,z),state,2);
        }
    }

    public static void cylinder(WorldServer w, BlockPos c, int radius, int height,
                                IBlockState state, boolean hollow) {
        int r2=radius*radius;
        for(int y=0;y<height;y++) for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            int d=x*x+z*z;
            if(d<=r2 && (!hollow || d>=(radius-1)*(radius-1)))
                w.setBlockState(c.add(x,y,z),state,2);
        }
    }

    public static void pyramid(WorldServer w, BlockPos c, int size, IBlockState state, boolean hollow) {
        for(int y=0;y<size;y++) {
            int radius=size-y-1;
            for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
                if(!hollow || Math.abs(x)==radius || Math.abs(z)==radius || y==0)
                    w.setBlockState(c.add(x,y,z),state,2);
            }
        }
    }

    public static void removeAbove(WorldServer w, BlockPos p, int height) {
        int max=Math.min(255,p.getY()+height);
        for(int y=p.getY()+1;y<=max;y++) for(int x=p.getX()-8;x<=p.getX()+8;x++)
            for(int z=p.getZ()-8;z<=p.getZ()+8;z++)
                w.setBlockState(new BlockPos(x,y,z),Blocks.air.getDefaultState(),2);
    }

    public static void removeBelow(WorldServer w, BlockPos p, int depth) {
        int min=Math.max(0,p.getY()-depth);
        for(int y=min;y<p.getY();y++) for(int x=p.getX()-8;x<=p.getX()+8;x++)
            for(int z=p.getZ()-8;z<=p.getZ()+8;z++)
                w.setBlockState(new BlockPos(x,y,z),Blocks.air.getDefaultState(),2);
    }

    public static int replaceNear(WorldServer w, BlockPos center, int radius,
                                  IBlockState from, IBlockState to) {
        int changed=0, r2=radius*radius;
        for(int x=-radius;x<=radius;x++) for(int y=-radius;y<=radius;y++) for(int z=-radius;z<=radius;z++) {
            if(x*x+y*y+z*z>r2) continue;
            BlockPos p=center.add(x,y,z);
            if(w.getBlockState(p).getBlock()==from.getBlock()) {
                w.setBlockState(p,to,2); changed++;
            }
        }
        return changed;
    }

    public static int drain(WorldServer w, BlockPos center, int radius) {
        int changed=0, r2=radius*radius;
        for(int x=-radius;x<=radius;x++) for(int y=-radius;y<=radius;y++) for(int z=-radius;z<=radius;z++) {
            if(x*x+y*y+z*z>r2) continue;
            BlockPos p=center.add(x,y,z);
            if(w.getBlockState(p).getBlock().getMaterial().isLiquid()) {
                w.setBlockState(p,Blocks.air.getDefaultState(),2); changed++;
            }
        }
        return changed;
    }

    public static int extinguish(WorldServer w, BlockPos center, int radius) {
        int changed=0;
        for(int x=-radius;x<=radius;x++) for(int y=-radius;y<=radius;y++) for(int z=-radius;z<=radius;z++) {
            BlockPos p=center.add(x,y,z);
            if(w.getBlockState(p).getBlock()==Blocks.fire) {
                w.setBlockState(p,Blocks.air.getDefaultState(),2); changed++;
            }
        }
        return changed;
    }
}
