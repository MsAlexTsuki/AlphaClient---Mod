package com.alpharip.staffmod.CommandPalette;

import com.alpharip.staffmod.WayPoints.WaypointFileUtils;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Persists pinned and recently used palette actions locally. */
public final class CommandPaletteStore {
    private static final CommandPaletteStore INSTANCE=new CommandPaletteStore();
    private static final int MAX_RECENT=12;
    private final Set<String> favorites=new LinkedHashSet<String>();
    private final List<String> recent=new ArrayList<String>();
    private boolean loaded,loadFailed;
    private CommandPaletteStore(){}

    public static CommandPaletteStore getInstance(){return INSTANCE;}

    public synchronized boolean isFavorite(String id){load();return id!=null&&favorites.contains(id);}
    public synchronized boolean isWritable(){load();return !loadFailed;}
    public synchronized boolean toggleFavorite(String id){
        load();
        if(id==null||id.trim().isEmpty()||loadFailed)return false;
        boolean added;
        if(favorites.contains(id)){favorites.remove(id);added=false;}
        else{favorites.add(id);added=true;}
        save();return added;
    }
    public synchronized Set<String> getFavoriteIds(){load();return Collections.unmodifiableSet(new LinkedHashSet<String>(favorites));}
    public synchronized List<String> getRecentIds(){load();return Collections.unmodifiableList(new ArrayList<String>(recent));}
    public synchronized int recentIndex(String id){load();int i=recent.indexOf(id);return i<0?Integer.MAX_VALUE:i;}
    public synchronized int favoriteIndex(String id){
        load();int i=0;for(String value:favorites){if(value.equals(id))return i;i++;}return Integer.MAX_VALUE;
    }
    public synchronized void recordUsed(String id){
        load();if(id==null||id.trim().isEmpty()||loadFailed)return;
        recent.remove(id);recent.add(0,id);
        while(recent.size()>MAX_RECENT)recent.remove(recent.size()-1);
        save();
    }

    private synchronized void load(){
        if(loaded)return;loaded=true;
        FileInputStream in=null;
        try{
            File source=file();if(!source.isFile())return;
            Properties properties=new Properties();in=new FileInputStream(source);properties.load(in);
            String pinned=properties.getProperty("favorites","");
            if(!pinned.trim().isEmpty())for(String id:pinned.split(","))if(!id.trim().isEmpty())favorites.add(id.trim());
            String used=properties.getProperty("recent","");
            if(!used.trim().isEmpty())for(String id:used.split(",")){
                String value=id.trim();
                if(!value.isEmpty()&&!recent.contains(value)&&recent.size()<MAX_RECENT)recent.add(value);
            }
        }catch(Exception e){
            loadFailed=true;
            System.err.println("[AlphaClient] Could not load command palette history; the file will be left unchanged: "+e.getMessage());
        }finally{if(in!=null)try{in.close();}catch(Exception ignored){}}
    }

    private synchronized void save(){
        if(loadFailed)return;
        File destination=file(),parent=destination.getParentFile();
        if(parent!=null&&!parent.exists()&&!parent.mkdirs()&&!parent.isDirectory())return;
        File temporary=new File(parent,destination.getName()+".tmp");FileOutputStream out=null;
        try{
            Properties properties=new Properties();
            properties.setProperty("favorites",join(favorites));
            properties.setProperty("recent",join(recent));
            out=new FileOutputStream(temporary);properties.store(out,"AlphaClient command palette");
            out.close();out=null;WaypointFileUtils.replaceAtomically(temporary,destination);
        }catch(Exception e){System.err.println("[AlphaClient] Could not save command palette history: "+e.getMessage());}
        finally{if(out!=null)try{out.close();}catch(Exception ignored){}}
    }

    private File file(){
        Minecraft mc=Minecraft.getMinecraft();
        File base=new File(mc==null?new File("."):mc.mcDataDir,"config/alphaclient");
        return new File(base,"command-palette.properties");
    }
    private static String join(Iterable<String> values){
        StringBuilder out=new StringBuilder();
        for(String value:values){if(out.length()>0)out.append(',');out.append(value);}
        return out.toString();
    }
}
