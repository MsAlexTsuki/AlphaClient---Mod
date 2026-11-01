// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.client.Minecraft;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Local text scripts restricted to Alpha World Edit commands. */
public final class WorldEditScriptStore {
    private WorldEditScriptStore(){}
    private static File directory(){
        File dir=new File(Minecraft.getMinecraft().mcDataDir,"AlphaWorldEdit/scripts");
        if(!dir.exists())dir.mkdirs();
        return dir;
    }
    private static File file(String name){
        if(name==null||!name.matches("[A-Za-z0-9_-]{1,48}"))return null;
        return new File(directory(),name+".acs");
    }
    public static void create(String name)throws IOException{
        File target=file(name);
        if(target==null)throw new IOException("Invalid script name.");
        if(target.exists())throw new IOException("Script already exists.");
        FileWriter writer=new FileWriter(target);
        try{
            writer.write("# Alpha World Edit script: "+name+"\n");
            writer.write("# One Alpha World Edit command per line; omit the //acwe prefix.\n");
            writer.write("# Example: set stone\n");
            writer.write("# Example: undo\n");
        }finally{writer.close();}
    }
    public static List<String> read(String name)throws IOException{
        File source=file(name);
        if(source==null||!source.isFile())throw new IOException("Script not found.");
        List<String> lines=new ArrayList<String>();
        BufferedReader reader=new BufferedReader(new FileReader(source));
        try{
            String line;
            while((line=reader.readLine())!=null){
                line=line.trim();
                if(!line.isEmpty()&&!line.startsWith("#"))lines.add(line);
                if(lines.size()>64)throw new IOException("Scripts are limited to 64 commands.");
            }
        }finally{reader.close();}
        return lines;
    }
}
