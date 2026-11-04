package com.alpharip.staffmod.waypoints;

import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Small local settings store for waypoint HUD and capture toggles. */
public final class WaypointSettings {
    private static final Properties VALUES=new Properties();
    private static boolean loaded;
    private static boolean loadFailed;
    private static boolean temporarilyHidden;
    private WaypointSettings(){}

    private static synchronized void load() {
        if(loaded)return; loaded=true;
        FileInputStream in=null;
        try {
            File file=file();
            if(file.isFile()){in=new FileInputStream(file);VALUES.load(in);}
        } catch(Exception e) {
            loadFailed=true;
            System.err.println("[AlphaClient] Could not load waypoint settings; saving is disabled to protect the existing file: "+e.getMessage());
        }
        finally { if(in!=null)try{in.close();}catch(Exception ignored){} }
    }
    private static File file() {
        Minecraft mc=Minecraft.getMinecraft();
        File base=new File(mc==null?new File("."):mc.mcDataDir,"config/alphaclient");
        if(!base.exists())base.mkdirs();
        return new File(base,"waypoints.properties");
    }
    public static synchronized boolean getBoolean(String key,boolean fallback) {
        load();
        String value=VALUES.getProperty(key);
        if("true".equalsIgnoreCase(value))return true;
        if("false".equalsIgnoreCase(value))return false;
        return fallback;
    }
    public static synchronized int getInt(String key,int fallback,int minimum,int maximum) {
        load();
        try { return Math.max(minimum,Math.min(maximum,Integer.parseInt(VALUES.getProperty(key,String.valueOf(fallback))))); }
        catch(NumberFormatException ignored) { return fallback; }
    }
    public static synchronized String getString(String key,String fallback) {
        load();
        String value=VALUES.getProperty(key,fallback);
        return value==null||value.trim().isEmpty()?fallback:value.trim();
    }
    public static synchronized boolean isTemporarilyHidden() { return temporarilyHidden; }
    public static synchronized void setTemporarilyHidden(boolean value) { temporarilyHidden=value; }

    public static synchronized void setBoolean(String key,boolean value) {
        load(); VALUES.setProperty(key,String.valueOf(value)); save();
    }
    public static synchronized void setInt(String key,int value) {
        load(); VALUES.setProperty(key,String.valueOf(value)); save();
    }
    public static synchronized void resetOptions() {
        load();
        VALUES.remove("labelSize");
        VALUES.remove("defaultIcon");
        VALUES.remove("blockHighlightEnabled");
        VALUES.remove("arrivalDetectionEnabled");
        VALUES.remove("maxWaypointRenderDistance");
        // Remove obsolete global controls; beam height is now distance-driven
        // and chroma is stored on each waypoint.
        VALUES.remove("beamHeight");
        VALUES.remove("chromaEnabled");
        save();
    }

    public static synchronized void setString(String key,String value) {
        load();
        if(value==null) VALUES.remove(key); else VALUES.setProperty(key,value);
        save();
    }
    private static void save() {
        if(loadFailed)return;
        File destination=file();
        File temporary=new File(destination.getParentFile(),destination.getName()+".tmp");
        FileOutputStream out=null;
        try {
            out=new FileOutputStream(temporary);
            VALUES.store(out,"AlphaClient waypoint settings");
            out.close();out=null;
            WaypointFileUtils.replaceAtomically(temporary,destination);
        } catch(Exception e) {
            System.err.println("[AlphaClient] Could not save waypoint settings: "+e.getMessage());
        } finally { if(out!=null)try{out.close();}catch(Exception ignored){} }
    }
}
