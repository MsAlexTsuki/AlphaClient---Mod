// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Named, local Alpha World Edit snapshots with their original world coordinates. */
public final class WorldEditSnapshotStore {
    private WorldEditSnapshotStore(){}

    private static File directory(){
        File dir=new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir,"AlphaWorldEdit/snapshots");
        if(!dir.exists())dir.mkdirs();
        return dir;
    }
    private static File file(String name){
        if(name==null||!name.matches("[A-Za-z0-9_-]{1,48}"))return null;
        return new File(directory(),name+".acsnap");
    }

    public static void save(String name,WorldServer world,WorldEditSelection.Bounds b)throws IOException{
        File target=file(name);
        if(target==null)throw new IOException("Invalid snapshot name.");
        IBlockState[][][] blocks=WorldEditOperations.copy(world,b);
        NBTTagCompound root=new NBTTagCompound();
        root.setInteger("OriginX",b.minX);root.setInteger("OriginY",b.minY);root.setInteger("OriginZ",b.minZ);
        root.setInteger("SizeX",b.sizeX());root.setInteger("SizeY",b.sizeY());root.setInteger("SizeZ",b.sizeZ());
        NBTTagList list=new NBTTagList();
        for(int x=0;x<blocks.length;x++)for(int y=0;y<blocks[x].length;y++)for(int z=0;z<blocks[x][y].length;z++){
            IBlockState state=blocks[x][y][z];
            ResourceLocation id=(ResourceLocation)Block.blockRegistry.getNameForObject(state.getBlock());
            NBTTagCompound entry=new NBTTagCompound();
            entry.setString("Id",id==null?"minecraft:air":id.toString());
            entry.setInteger("Meta",state.getBlock().getMetaFromState(state));
            list.appendTag(entry);
        }
        root.setTag("Blocks",list);
        FileOutputStream out=new FileOutputStream(target);
        try{CompressedStreamTools.writeCompressed(root,out);}finally{out.close();}
    }

    public static Snapshot load(String name)throws IOException{
        File source=file(name);
        if(source==null||!source.isFile())throw new IOException("Snapshot not found.");
        FileInputStream in=new FileInputStream(source);
        NBTTagCompound root;
        try{root=CompressedStreamTools.readCompressed(in);}finally{in.close();}
        int sx=root.getInteger("SizeX"),sy=root.getInteger("SizeY"),sz=root.getInteger("SizeZ");
        if(sx<1||sy<1||sz<1||(long)sx*sy*sz>32768L)throw new IOException("Snapshot dimensions are invalid or too large.");
        NBTTagList list=root.getTagList("Blocks",10);
        if(list.tagCount()!=sx*sy*sz)throw new IOException("Snapshot block data is incomplete.");
        IBlockState[][][] blocks=new IBlockState[sx][sy][sz];int i=0;
        for(int x=0;x<sx;x++)for(int y=0;y<sy;y++)for(int z=0;z<sz;z++){
            NBTTagCompound entry=list.getCompoundTagAt(i++);
            Block block=Block.getBlockFromName(entry.getString("Id"));
            if(block==null)block=net.minecraft.init.Blocks.air;
            blocks[x][y][z]=block.getStateFromMeta(Math.max(0,Math.min(15,entry.getInteger("Meta"))));
        }
        return new Snapshot(new BlockPos(root.getInteger("OriginX"),root.getInteger("OriginY"),root.getInteger("OriginZ")),blocks);
    }

    public static List<String> list(){
        List<String> names=new ArrayList<String>();
        File[] files=directory().listFiles();
        if(files!=null)for(File f:files)if(f.isFile()&&f.getName().endsWith(".acsnap"))
            names.add(f.getName().substring(0,f.getName().length()-7));
        Collections.sort(names);return names;
    }

    public static final class Snapshot{
        public final BlockPos origin;
        public final IBlockState[][][] blocks;
        private Snapshot(BlockPos origin,IBlockState[][][] blocks){this.origin=origin;this.blocks=blocks;}
        public WorldEditSelection.Bounds bounds(){
            return new WorldEditSelection.Bounds(origin.getX(),origin.getY(),origin.getZ(),
                    origin.getX()+blocks.length-1,origin.getY()+blocks[0].length-1,origin.getZ()+blocks[0][0].length-1);
        }
    }
}
