// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

/** Independent region and primitive-generation operations for Alpha World Edit. */
public final class WorldEditAdvancedOperations {
    private WorldEditAdvancedOperations() { }

    private static void record(WorldServer w,int minX,int minY,int minZ,int maxX,int maxY,int maxZ) {
        int lowY=Math.max(0,minY), highY=Math.min(255,maxY);
        if(lowY>highY)return;
        WorldEditHistory.record(w,new WorldEditSelection.Bounds(minX,lowY,minZ,maxX,highY,maxZ));
    }

    public static void line(WorldServer w, BlockPos a, BlockPos b, IBlockState state) {
        record(w,Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()),Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
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
        record(w,(b.minX+b.maxX)/2,(b.minY+b.maxY)/2,(b.minZ+b.maxZ)/2,(b.minX+b.maxX)/2,(b.minY+b.maxY)/2,(b.minZ+b.maxZ)/2);
        w.setBlockState(new BlockPos((b.minX+b.maxX)/2, (b.minY+b.maxY)/2,
                (b.minZ+b.maxZ)/2), state, 2);
    }

    public static void overlay(WorldServer w, WorldEditSelection.Bounds b, IBlockState state) {
        record(w,b.minX,b.minY,b.minZ,b.maxX,Math.min(255,b.maxY+1),b.maxZ);
        for (int x=b.minX;x<=b.maxX;x++) for (int z=b.minZ;z<=b.maxZ;z++) {
            BlockPos top=w.getTopSolidOrLiquidBlock(new BlockPos(x,b.maxY,z));
            if (top.getY() >= b.minY && top.getY() < b.maxY && top.getY() < 255)
                w.setBlockState(top.up(), state, 2);
        }
    }

    public static void naturalize(WorldServer w, WorldEditSelection.Bounds b) {
        record(w,b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ);
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
        record(w,c.getX()-rx,c.getY()-ry,c.getZ()-rz,c.getX()+rx,c.getY()+ry,c.getZ()+rz);
        for(int x=-rx;x<=rx;x++) for(int y=Math.max(-ry,-c.getY());y<=Math.min(ry,255-c.getY());y++) for(int z=-rz;z<=rz;z++) {
            double d=(double)x*x/(rx*rx)+(double)y*y/(ry*ry)+(double)z*z/(rz*rz);
            if(d<=1.0 && (!hollow || d>=0.75))
                w.setBlockState(c.add(x,y,z),state,2);
        }
    }

    public static void cylinder(WorldServer w, BlockPos c, int radius, int height,
                                IBlockState state, boolean hollow) {
        record(w,c.getX()-radius,c.getY(),c.getZ()-radius,c.getX()+radius,c.getY()+height-1,c.getZ()+radius);
        int r2=radius*radius;
        for(int y=0;y<height && c.getY()+y<=255;y++) for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            int d=x*x+z*z;
            if(d<=r2 && (!hollow || d>=(radius-1)*(radius-1)))
                w.setBlockState(c.add(x,y,z),state,2);
        }
    }

    public static void pyramid(WorldServer w, BlockPos c, int size, IBlockState state, boolean hollow) {
        record(w,c.getX()-size+1,c.getY(),c.getZ()-size+1,c.getX()+size-1,c.getY()+size-1,c.getZ()+size-1);
        for(int y=0;y<size && c.getY()+y<=255;y++) {
            int radius=size-y-1;
            for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
                if(!hollow || Math.abs(x)==radius || Math.abs(z)==radius || y==0)
                    w.setBlockState(c.add(x,y,z),state,2);
            }
        }
    }

    public static void removeAbove(WorldServer w, BlockPos p, int height) {
        record(w,p.getX()-8,p.getY()+1,p.getZ()-8,p.getX()+8,Math.min(255,p.getY()+height),p.getZ()+8);
        int max=Math.min(255,p.getY()+height);
        for(int y=p.getY()+1;y<=max;y++) for(int x=p.getX()-8;x<=p.getX()+8;x++)
            for(int z=p.getZ()-8;z<=p.getZ()+8;z++)
                w.setBlockState(new BlockPos(x,y,z),Blocks.air.getDefaultState(),2);
    }

    public static void removeBelow(WorldServer w, BlockPos p, int depth) {
        record(w,p.getX()-8,Math.max(0,p.getY()-depth),p.getZ()-8,p.getX()+8,p.getY()-1,p.getZ()+8);
        int min=Math.max(0,p.getY()-depth);
        for(int y=min;y<p.getY();y++) for(int x=p.getX()-8;x<=p.getX()+8;x++)
            for(int z=p.getZ()-8;z<=p.getZ()+8;z++)
                w.setBlockState(new BlockPos(x,y,z),Blocks.air.getDefaultState(),2);
    }

    public static int replaceNear(WorldServer w, BlockPos center, int radius,
                                  IBlockState from, IBlockState to) {
        record(w,center.getX()-radius,center.getY()-radius,center.getZ()-radius,center.getX()+radius,center.getY()+radius,center.getZ()+radius);
        int changed=0, r2=radius*radius;
        for(int x=-radius;x<=radius;x++) for(int y=Math.max(-radius,-center.getY());y<=Math.min(radius,255-center.getY());y++) for(int z=-radius;z<=radius;z++) {
            if(x*x+y*y+z*z>r2) continue;
            BlockPos p=center.add(x,y,z);
            if(w.getBlockState(p).getBlock()==from.getBlock()) {
                w.setBlockState(p,to,2); changed++;
            }
        }
        return changed;
    }

    public static int drain(WorldServer w, BlockPos center, int radius) {
        record(w,center.getX()-radius,center.getY()-radius,center.getZ()-radius,center.getX()+radius,center.getY()+radius,center.getZ()+radius);
        int changed=0, r2=radius*radius;
        for(int x=-radius;x<=radius;x++) for(int y=Math.max(-radius,-center.getY());y<=Math.min(radius,255-center.getY());y++) for(int z=-radius;z<=radius;z++) {
            if(x*x+y*y+z*z>r2) continue;
            BlockPos p=center.add(x,y,z);
            if(w.getBlockState(p).getBlock().getMaterial().isLiquid()) {
                w.setBlockState(p,Blocks.air.getDefaultState(),2); changed++;
            }
        }
        return changed;
    }

    public static void smooth(WorldServer w, WorldEditSelection.Bounds b, int iterations) {
        record(w,b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ);
        for(int pass=0;pass<iterations;pass++) {
            java.util.Map<BlockPos,Integer> heights=new java.util.HashMap<BlockPos,Integer>();
            for(int x=b.minX;x<=b.maxX;x++) for(int z=b.minZ;z<=b.maxZ;z++) {
                int total=0,count=0;
                for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
                    int nx=x+dx,nz=z+dz;
                    if(nx<b.minX||nx>b.maxX||nz<b.minZ||nz>b.maxZ) continue;
                    int h=w.getTopSolidOrLiquidBlock(new BlockPos(nx,b.maxY,nz)).getY();
                    total+=h;count++;
                }
                if(count>0) heights.put(new BlockPos(x,0,z),total/count);
            }
            for(java.util.Map.Entry<BlockPos,Integer> entry:heights.entrySet()) {
                int x=entry.getKey().getX(),z=entry.getKey().getZ(),target=entry.getValue();
                int current=w.getTopSolidOrLiquidBlock(new BlockPos(x,b.maxY,z)).getY();
                if(current>target) for(int y=target+1;y<=current&&y<=b.maxY;y++)
                    w.setBlockState(new BlockPos(x,y,z),Blocks.air.getDefaultState(),2);
                else if(current<target) {
                    IBlockState fill=w.getBlockState(new BlockPos(x,current,z));
                    for(int y=current+1;y<=target&&y<=b.maxY;y++)
                        w.setBlockState(new BlockPos(x,y,z),fill,2);
                }
            }
        }
    }

    public static IBlockState[][][] rotateY(IBlockState[][][] input, int degrees) {
        int turns=((degrees%360)+360)%360/90;
        IBlockState[][][] result=input;
        for(int t=0;t<turns;t++) {
            int sx=result.length, sy=result[0].length, sz=result[0][0].length;
            IBlockState[][][] rotated=new IBlockState[sz][sy][sx];
            for(int x=0;x<sx;x++) for(int y=0;y<sy;y++) for(int z=0;z<sz;z++)
                rotated[sz-1-z][y][x]=result[x][y][z];
            result=rotated;
        }
        return result;
    }

    public static IBlockState[][][] flip(IBlockState[][][] input, String axis) {
        int sx=input.length, sy=input[0].length, sz=input[0][0].length;
        IBlockState[][][] result=new IBlockState[sx][sy][sz];
        for(int x=0;x<sx;x++) for(int y=0;y<sy;y++) for(int z=0;z<sz;z++) {
            int tx="x".equals(axis)?sx-1-x:x;
            int ty="y".equals(axis)?sy-1-y:y;
            int tz="z".equals(axis)?sz-1-z:z;
            result[tx][ty][tz]=input[x][y][z];
        }
        return result;
    }

    public static int removeNear(WorldServer w, BlockPos center, int radius, IBlockState target) {
        record(w,center.getX()-radius,center.getY()-radius,center.getZ()-radius,center.getX()+radius,center.getY()+radius,center.getZ()+radius);
        int changed=0, r2=radius*radius;
        for(int x=-radius;x<=radius;x++) for(int y=Math.max(-radius,-center.getY());y<=Math.min(radius,255-center.getY());y++) for(int z=-radius;z<=radius;z++) {
            if(x*x+y*y+z*z>r2) continue;
            BlockPos p=center.add(x,y,z);
            if(w.getBlockState(p).getBlock()==target.getBlock()) {
                w.setBlockState(p,Blocks.air.getDefaultState(),2); changed++;
            }
        }
        return changed;
    }

    public static int changeSurface(WorldServer w, WorldEditSelection.Bounds b, int mode) {
        record(w,b.minX,b.minY,b.minZ,b.maxX,Math.min(255,b.maxY+1),b.maxZ);
        int changed=0;
        for(int x=b.minX;x<=b.maxX;x++) for(int z=b.minZ;z<=b.maxZ;z++) {
            BlockPos top=w.getTopSolidOrLiquidBlock(new BlockPos(x,b.maxY,z));
            if(top.getY()<b.minY || top.getY()>b.maxY) continue;
            BlockPos above=top.up();
            if(mode==0 && top.getY()<255 && w.isAirBlock(above) && w.getBlockState(top).getBlock().isFullBlock()) {
                w.setBlockState(above,Blocks.snow_layer.getDefaultState(),2); changed++;
            } else if(mode==1) {
                if(w.getBlockState(above).getBlock()==Blocks.snow_layer || w.getBlockState(top).getBlock()==Blocks.ice) {
                    w.setBlockState(above,Blocks.air.getDefaultState(),2);
                    if(w.getBlockState(top).getBlock()==Blocks.ice) w.setBlockState(top,Blocks.water.getDefaultState(),2);
                    changed++;
                }
            } else if(mode==2 && w.getBlockState(top).getBlock()==Blocks.dirt) {
                w.setBlockState(top,Blocks.grass.getDefaultState(),2); changed++;
            }
        }
        return changed;
    }

    public static int fixLiquid(WorldServer w, WorldEditSelection.Bounds b, boolean lava) {
        record(w,b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ);
        int changed=0;
        net.minecraft.block.Block flowing=lava?Blocks.flowing_lava:Blocks.flowing_water;
        IBlockState source=lava?Blocks.lava.getDefaultState():Blocks.water.getDefaultState();
        for(int x=b.minX;x<=b.maxX;x++) for(int y=b.minY;y<=b.maxY;y++) for(int z=b.minZ;z<=b.maxZ;z++) {
            BlockPos p=new BlockPos(x,y,z);
            if(w.getBlockState(p).getBlock()==flowing) { w.setBlockState(p,source,2); changed++; }
        }
        return changed;
    }

    public static int extinguish(WorldServer w, BlockPos center, int radius) {
        record(w,center.getX()-radius,center.getY()-radius,center.getZ()-radius,center.getX()+radius,center.getY()+radius,center.getZ()+radius);
        int changed=0;
        for(int x=-radius;x<=radius;x++) for(int y=Math.max(-radius,-center.getY());y<=Math.min(radius,255-center.getY());y++) for(int z=-radius;z<=radius;z++) {
            BlockPos p=center.add(x,y,z);
            if(w.getBlockState(p).getBlock()==Blocks.fire) {
                w.setBlockState(p,Blocks.air.getDefaultState(),2); changed++;
            }
        }
        return changed;
    }
}
