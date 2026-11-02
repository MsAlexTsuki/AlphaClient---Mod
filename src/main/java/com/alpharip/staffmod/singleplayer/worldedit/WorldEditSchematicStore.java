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

/** AlphaClient schematic storage and legacy MCEdit/WorldEdit 1.8.9 schematic importer. */
public final class WorldEditSchematicStore {
    private static final long MAX_SCHEMATIC_BLOCKS = 65536L;

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

    private static File legacyFile(String name) {
        if(name==null)return null;
        String base=name.toLowerCase(java.util.Locale.ROOT).endsWith(".schematic")
                ?name.substring(0,name.length()-10):name;
        if(!base.matches("[A-Za-z0-9_-]{1,48}"))return null;
        return new File(directory(),base+".schematic");
    }

    public static void save(String name,IBlockState[][][] blocks) throws IOException {
        File target=file(name);
        if(target==null||blocks==null||blocks.length==0||blocks[0].length==0||blocks[0][0].length==0)
            throw new IOException("Invalid schematic name or empty clipboard.");
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

    /** Loads an AlphaClient .acwe file or a classic compressed MCEdit .schematic file. */
    public static IBlockState[][][] load(String name) throws IOException {
        if(name!=null&&name.toLowerCase(java.util.Locale.ROOT).endsWith(".schematic"))
            return loadLegacy(name);
        File source=file(name);
        if(source==null||!source.isFile()){
            File legacy=legacyFile(name);
            if(legacy!=null&&legacy.isFile())return loadLegacy(name);
            throw new IOException("Schematic not found.");
        }
        FileInputStream in=new FileInputStream(source);
        NBTTagCompound root;
        try{root=CompressedStreamTools.readCompressed(in);}finally{in.close();}
        int sx=root.getInteger("SizeX"),sy=root.getInteger("SizeY"),sz=root.getInteger("SizeZ");
        validateDimensions(sx,sy,sz);
        NBTTagList list=root.getTagList("Blocks",10);
        if((long)list.tagCount()!=(long)sx*sy*sz)throw new IOException("Schematic block data is incomplete.");
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

    /** Reads the classic 1.8.9-era MCEdit/WorldEdit schematic layout (Blocks, Data, AddBlocks). */
    private static IBlockState[][][] loadLegacy(String name) throws IOException {
        File source=legacyFile(name);
        if(source==null||!source.isFile())throw new IOException("1.8.9 .schematic file not found in AlphaWorldEdit/schematics.");
        FileInputStream in=new FileInputStream(source);
        NBTTagCompound root;
        try{root=CompressedStreamTools.readCompressed(in);}finally{in.close();}

        int sx=root.getShort("Width")&0xFFFF;
        int sy=root.getShort("Height")&0xFFFF;
        int sz=root.getShort("Length")&0xFFFF;
        validateDimensions(sx,sy,sz);

        byte[] ids=root.getByteArray("Blocks");
        byte[] data=root.getByteArray("Data");
        int volume=sx*sy*sz;
        if(ids.length!=volume||data.length!=volume)
            throw new IOException("The .schematic Blocks or Data array has an invalid length.");

        byte[] add=root.hasKey("AddBlocks")?root.getByteArray("AddBlocks"):new byte[0];
        if(add.length>0&&add.length<(volume+1)/2)
            throw new IOException("The .schematic AddBlocks array is incomplete.");

        IBlockState[][][] blocks=new IBlockState[sx][sy][sz];
        // MCEdit stores X as the fastest-changing coordinate, then Z, then Y.
        for(int y=0;y<sy;y++)for(int z=0;z<sz;z++)for(int x=0;x<sx;x++){
            int index=y*sx*sz+z*sx+x;
            int blockId=ids[index]&0xFF;
            if(add.length>0){
                int packed=add[index>>1]&0xFF;
                int high=(index&1)==0?(packed&0x0F):((packed>>4)&0x0F);
                blockId|=high<<8;
            }
            Block block=Block.getBlockById(blockId);
            if(block==null)
                throw new IOException("Unsupported legacy block ID "+blockId+" at schematic index "+index+".");
            int metadata=data[index]&0x0F;
            blocks[x][y][z]=block.getStateFromMeta(metadata);
        }
        return blocks;
    }

    private static void validateDimensions(int sx,int sy,int sz) throws IOException {
        if(sx<1||sy<1||sz<1||(long)sx*sy*sz>MAX_SCHEMATIC_BLOCKS)
            throw new IOException("Schematic dimensions are invalid or exceed the 65536-block limit.");
    }

    public static boolean delete(String name) {
        if(name!=null&&name.toLowerCase(java.util.Locale.ROOT).endsWith(".schematic")){
            File legacy=legacyFile(name);
            return legacy!=null&&legacy.isFile()&&legacy.delete();
        }
        File target=file(name);
        return target!=null&&target.isFile()&&target.delete();
    }

    public static List<String> list() {
        List<String> names=new ArrayList<String>();
        File[] files=directory().listFiles();
        if(files!=null)for(File f:files)if(f.isFile()){
            if(f.getName().endsWith(".acwe"))names.add(f.getName().substring(0,f.getName().length()-5));
            else if(f.getName().endsWith(".schematic"))names.add(f.getName());
        }
        java.util.Collections.sort(names);
        return names;
    }
}
