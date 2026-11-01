// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** AlphaClient-owned compressed NBT schematic storage. */
public final class WorldEditSchematicStore {
    private WorldEditSchematicStore() { }

    private static File directory() {
        File dir=new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir,"AlphaWorldEdit/schematics");
        if(!dir.exists())dir.mkdirs();
        return dir;
    }

    private static File file(String name) {
        if(name==null||!name.matches("[A-Za-z0-9_-]{1,48}"))return null;
        return new File(directory(),name+".acwe");
    }

    public static void save(String name,IBlockState[][][] blocks) throws IOException {
        File target=file(name);
        if(target==null||blocks==null)throw new IOException("Invalid schematic name or empty clipboard.");
        NBTTagCompound root=new NBTTagCompound();
        root.setInteger("SizeX",blocks.length);
        root.setInteger("SizeY",blocks[0].length);
        root.setInteger("SizeZ",blocks[0][0].length);
        NBTTagList list=new NBTTagList();
        for(int x=0;x<blocks.length;x++)for(int y=0;y<blocks[x].length;y++)for(int z=0;z<blocks[x][y].length;z++){
            IBlockState state=blocks[x][y][z];
            NBTTagCompound entry=new NBTTagCompound();
            ResourceLocation id=(ResourceLocation)Block.blockRegistry.getNameForObject(state.getBlock());
            entry.setString("Id",id==null?"minecraft:air":id.toString());
            entry.setInteger("Meta",state.getBlock().getMetaFromState(state));
            list.appendTag(entry);
        }
        root.setTag("Blocks",list);
        FileOutputStream out=new FileOutputStream(target);
        try{CompressedStreamTools.writeCompressed(root,out);}finally{out.close();}
    }

    public static IBlockState[][][] load(String name) throws IOException {
        File source=file(name);
        if(source==null||!source.isFile())throw new IOException("Schematic not found.");
        FileInputStream in=new FileInputStream(source);
        NBTTagCompound root;
        try{root=CompressedStreamTools.readCompressed(in);}finally{in.close();}
        int sx=root.getInteger("SizeX"),sy=root.getInteger("SizeY"),sz=root.getInteger("SizeZ");
        if(sx<1||sy<1||sz<1||(long)sx*sy*sz>32768L)throw new IOException("Schematic dimensions are invalid or too large.");
        NBTTagList list=root.getTagList("Blocks",10);
        if(list.tagCount()!=sx*sy*sz)throw new IOException("Schematic block data is incomplete.");
        IBlockState[][][] blocks=new IBlockState[sx][sy][sz];
        int i=0;
        for(int x=0;x<sx;x++)for(int y=0;y<sy;y++)for(int z=0;z<sz;z++){
            NBTTagCompound entry=list.getCompoundTagAt(i++);
            Block block=Block.getBlockFromName(entry.getString("Id"));
            if(block==null)block=net.minecraft.init.Blocks.air;
            int meta=Math.max(0,Math.min(15,entry.getInteger("Meta")));
            blocks[x][y][z]=block.getStateFromMeta(meta);
        }
        return blocks;
    }

    public static boolean delete(String name) {
        File target=file(name);
        return target!=null&&target.isFile()&&target.delete();
    }

    public static List<String> list() {
        List<String> names=new ArrayList<String>();
        File[] files=directory().listFiles();
        if(files!=null)for(File f:files)if(f.isFile()&&f.getName().endsWith(".acwe"))
            names.add(f.getName().substring(0,f.getName().length()-5));
        java.util.Collections.sort(names);
        return names;
    }
}
