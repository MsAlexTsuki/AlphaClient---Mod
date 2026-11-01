// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.List;
import java.util.Random;

/** Additional bounded Alpha World Edit operations. */
public final class WorldEditFeatureOperations {
    private WorldEditFeatureOperations() { }

    private static void record(WorldServer world, WorldEditSelection.Bounds b) {
        WorldEditHistory.record(world,b);
    }

    public static void curve(WorldServer world, BlockPos start, BlockPos control,
                             BlockPos end, IBlockState state) {
        int dx=Math.max(Math.abs(start.getX()-control.getX()),Math.max(Math.abs(control.getX()-end.getX()),Math.abs(start.getX()-end.getX())));
        int dy=Math.max(Math.abs(start.getY()-control.getY()),Math.max(Math.abs(control.getY()-end.getY()),Math.abs(start.getY()-end.getY())));
        int dz=Math.max(Math.abs(start.getZ()-control.getZ()),Math.max(Math.abs(control.getZ()-end.getZ()),Math.abs(start.getZ()-end.getZ())));
        int steps=Math.max(1,Math.max(dx,Math.max(dy,dz))*2);
        record(world,new WorldEditSelection.Bounds(
                Math.min(start.getX(),Math.min(control.getX(),end.getX())),
                Math.min(start.getY(),Math.min(control.getY(),end.getY())),
                Math.min(start.getZ(),Math.min(control.getZ(),end.getZ())),
                Math.max(start.getX(),Math.max(control.getX(),end.getX())),
                Math.max(start.getY(),Math.max(control.getY(),end.getY())),
                Math.max(start.getZ(),Math.max(control.getZ(),end.getZ()))));
        for(int i=0;i<=steps;i++) {
            double t=(double)i/steps, u=1.0-t;
            int x=(int)Math.round(u*u*start.getX()+2*u*t*control.getX()+t*t*end.getX());
            int y=(int)Math.round(u*u*start.getY()+2*u*t*control.getY()+t*t*end.getY());
            int z=(int)Math.round(u*u*start.getZ()+2*u*t*control.getZ()+t*t*end.getZ());
            if(y>=0&&y<=255)world.setBlockState(new BlockPos(x,y,z),state,2);
        }
    }

    /** Applies a bounded sinusoidal height displacement to the selected terrain columns. */
    public static void deform(WorldServer world, WorldEditSelection.Bounds b, double amplitude, double frequency) {
        record(world,b);
        IBlockState[][] columns=new IBlockState[b.sizeX()][b.sizeZ()];
        int[][] heights=new int[b.sizeX()][b.sizeZ()];
        for(int x=b.minX;x<=b.maxX;x++)for(int z=b.minZ;z<=b.maxZ;z++) {
            int top=-1;
            for(int y=b.maxY;y>=b.minY;y--)if(!world.isAirBlock(new BlockPos(x,y,z))){top=y;break;}
            heights[x-b.minX][z-b.minZ]=top;
            if(top>=b.minY)columns[x-b.minX][z-b.minZ]=world.getBlockState(new BlockPos(x,top,z));
        }
        for(int x=b.minX;x<=b.maxX;x++)for(int z=b.minZ;z<=b.maxZ;z++) {
            int old=heights[x-b.minX][z-b.minZ];
            if(old<b.minY)continue;
            double wave=Math.sin((x-b.minX)*frequency)*Math.cos((z-b.minZ)*frequency);
            int target=Math.max(b.minY,Math.min(b.maxY,old+(int)Math.round(wave*amplitude)));
            IBlockState topState=columns[x-b.minX][z-b.minZ];
            if(target<old)for(int y=target+1;y<=old;y++)world.setBlockState(new BlockPos(x,y,z),Blocks.air.getDefaultState(),2);
            else if(target>old)for(int y=old+1;y<=target;y++)world.setBlockState(new BlockPos(x,y,z),topState,2);
        }
    }

    public static int forest(WorldServer world, WorldEditSelection.Bounds b, int density, Random random) {
        record(world,b);
        int planted=0;
        WorldGenTrees trees=new WorldGenTrees(true);
        int attempts=Math.min(256,Math.max(1,b.sizeX()*b.sizeZ()/Math.max(1,density)));
        for(int i=0;i<attempts;i++) {
            int x=b.minX+random.nextInt(b.sizeX()),z=b.minZ+random.nextInt(b.sizeZ());
            BlockPos ground=world.getTopSolidOrLiquidBlock(new BlockPos(x,b.maxY,z));
            if(ground.getY()<b.minY||ground.getY()+8>b.maxY)continue;
            if(trees.generate(world,random,ground))planted++;
        }
        return planted;
    }

    public static int flora(WorldServer world, WorldEditSelection.Bounds b, int attempts, Random random) {
        record(world,b);
        int planted=0;
        for(int i=0;i<attempts;i++) {
            int x=b.minX+random.nextInt(b.sizeX()),z=b.minZ+random.nextInt(b.sizeZ());
            BlockPos ground=world.getTopSolidOrLiquidBlock(new BlockPos(x,b.maxY,z));
            BlockPos place=ground;
            if(ground.getY()<b.minY||ground.getY()>=b.maxY||!world.isAirBlock(place))continue;
            if(random.nextInt(5)==0) {
                IBlockState flower=(random.nextBoolean()?Blocks.yellow_flower:Blocks.red_flower).getDefaultState();
                if(world.isAirBlock(place)&&flower.getBlock().canPlaceBlockAt(world,place)){world.setBlockState(place,flower,2);planted++;}
            } else {
                IBlockState grass=Blocks.tallgrass.getDefaultState();
                if(world.isAirBlock(place)&&grass.getBlock().canPlaceBlockAt(world,place)){world.setBlockState(place,grass,2);planted++;}
            }
        }
        return planted;
    }

    public static int pumpkins(WorldServer world, WorldEditSelection.Bounds b, int attempts, Random random) {
        record(world,b);
        int planted=0;
        for(int i=0;i<attempts;i++) {
            int x=b.minX+random.nextInt(b.sizeX()),z=b.minZ+random.nextInt(b.sizeZ());
            BlockPos ground=world.getTopSolidOrLiquidBlock(new BlockPos(x,b.maxY,z));
            BlockPos place=ground;
            if(ground.getY()<b.minY||ground.getY()>=b.maxY||!world.isAirBlock(place))continue;
            IBlockState pumpkin=Blocks.pumpkin.getStateFromMeta(random.nextInt(4));
            if(pumpkin.getBlock().canPlaceBlockAt(world,place)){world.setBlockState(place,pumpkin,2);planted++;}
        }
        return planted;
    }

    public static int butcher(WorldServer world, BlockPos center, int radius, boolean animals) {
        AxisAlignedBB box=new AxisAlignedBB(center.getX()-radius,Math.max(0,center.getY()-radius),center.getZ()-radius,
                center.getX()+radius+1,Math.min(256,center.getY()+radius+1),center.getZ()+radius+1);
        List<Entity> entities=world.getEntitiesWithinAABB(Entity.class,box);
        int removed=0;
        for(Entity entity:entities) {
            if(entity instanceof EntityLivingBase && !(entity instanceof net.minecraft.entity.player.EntityPlayer)) {
                boolean hostile=entity instanceof IMob;
                boolean passive=entity instanceof EntityAnimal;
                if(hostile|| (animals&&passive)) { entity.setDead();removed++; }
            }
        }
        return removed;
    }
}
