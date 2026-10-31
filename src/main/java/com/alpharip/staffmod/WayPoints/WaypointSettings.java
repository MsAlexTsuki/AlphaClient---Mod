package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Small local settings store for waypoint HUD and capture toggles. */
public final class WaypointSettings {
    private static final Properties VALUES=new Properties();
    private static boolean loaded;
    private WaypointSettings(){}

    private static synchronized void load() {
        if(loaded)return; loaded=true;
        FileInputStream in=null;
        try {
            File file=file();
            if(file.isFile()){in=new FileInputStream(file);VALUES.load(in);}
        } catch(Exception e) { System.err.println("[AlphaClient] Could not load waypoint settings: "+e.getMessage()); }
        finally { if(in!=null)try{in.close();}catch(Exception ignored){} }
    }
    private static File file() {
        Minecraft mc=Minecraft.getMinecraft();
        File base=new File(mc==null?new File("."):mc.mcDataDir,"config/alphaclient");
        if(!base.exists())base.mkdirs();
        return new File(base,"waypoints.properties");
    }
    public static synchronized boolean getBoolean(String key,boolean fallback) {
        load(); return Boolean.parseBoolean(VALUES.getProperty(key,String.valueOf(fallback)));
    }
    public static synchronized void setBoolean(String key,boolean value) {
        load(); VALUES.setProperty(key,String.valueOf(value));
        FileOutputStream out=null;
        try { out=new FileOutputStream(file()); VALUES.store(out,"AlphaClient waypoint settings"); }
        catch(Exception e) { System.err.println("[AlphaClient] Could not save waypoint settings: "+e.getMessage()); }
        finally { if(out!=null)try{out.close();}catch(Exception ignored){} }
    }
}
