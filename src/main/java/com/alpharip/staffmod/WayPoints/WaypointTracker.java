package com.alpharip.staffmod.WayPoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tracks visited coordinates and marks waypoints visited when the player arrives. */
public final class WaypointTracker {
    private static final WaypointTracker INSTANCE=new WaypointTracker();
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Type HISTORY_TYPE=new TypeToken<ArrayList<CoordinateEntry>>(){}.getType();
    private static final int MAX_HISTORY=100;
    private final List<CoordinateEntry> history=new ArrayList<CoordinateEntry>();
    private boolean historyLoaded;
    private String lastServer="";
    private int lastDimension=Integer.MIN_VALUE;
    private double lastX,lastY,lastZ;
    private long lastSampleAt;

    private WaypointTracker(){}
    public static WaypointTracker getInstance(){return INSTANCE;}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null||mc.thePlayer==null||mc.theWorld==null)return;
        recordPosition(mc);
        if(!WaypointSettings.getBoolean("arrivalDetectionEnabled",true))return;
        WaypointManager manager=WaypointManager.getInstance();
        boolean changed=false;
        for(StaffWaypoint point:manager.getForCurrentWorld()) {
            if(point.isVisited())continue;
            double dx=mc.thePlayer.posX-point.getX();
            double dy=mc.thePlayer.posY-point.getY();
            double dz=mc.thePlayer.posZ-point.getZ();
            if(dx*dx+dy*dy+dz*dz<=9.0D) {
                point.setVisited(true);
                mc.thePlayer.addChatMessage(new ChatComponentText("§aWaypoint reached: §f"+point.getLabel()));
                changed=true;
            }
        }
        if(changed)manager.saveChanges();
    }

    private void recordPosition(Minecraft mc) {
        String server=ServerWaypointIntegration.currentServerKey(mc);
        int dimension=mc.thePlayer.dimension;
        double x=Math.floor(mc.thePlayer.posX),y=Math.floor(mc.thePlayer.posY),z=Math.floor(mc.thePlayer.posZ);
        long now=System.currentTimeMillis();
        synchronized(this) {
            loadHistory();
            boolean contextChanged=!server.equals(lastServer)||dimension!=lastDimension;
            double dx=x-lastX,dy=y-lastY,dz=z-lastZ;
            if(!contextChanged&&(now-lastSampleAt<10000L||dx*dx+dy*dy+dz*dz<1024.0D))return;
            lastServer=server;lastDimension=dimension;lastX=x;lastY=y;lastZ=z;lastSampleAt=now;
            for(CoordinateEntry old:history) {
                if(server.equals(old.serverKey)&&dimension==old.dimensionId) {
                    double ox=old.x-x,oy=old.y-y,oz=old.z-z;
                    if(ox*ox+oy*oy+oz*oz<64.0D)return;
                }
            }
            history.add(0,new CoordinateEntry(server,dimension,x,y,z,now));
            while(history.size()>MAX_HISTORY)history.remove(history.size()-1);
            saveHistory();
        }
    }

    public synchronized List<CoordinateEntry> getHistoryForCurrentWorld() {
        loadHistory();
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null||mc.thePlayer==null)return Collections.emptyList();
        String server=ServerWaypointIntegration.currentServerKey(mc);
        List<CoordinateEntry> result=new ArrayList<CoordinateEntry>();
        for(CoordinateEntry entry:history)
            if(entry!=null&&server.equals(entry.serverKey)&&entry.dimensionId==mc.thePlayer.dimension)result.add(entry);
        return Collections.unmodifiableList(result);
    }

    public synchronized CoordinateEntry getCurrentHistoryEntry(int index) {
        List<CoordinateEntry> current=getHistoryForCurrentWorld();
        return index<0||index>=current.size()?null:current.get(index);
    }

    private void loadHistory() {
        if(historyLoaded)return;
        historyLoaded=true;
        File file=historyFile();
        if(!file.isFile())return;
        InputStreamReader reader=null;
        try {
            reader=new InputStreamReader(new FileInputStream(file),"UTF-8");
            List<CoordinateEntry> saved=GSON.fromJson(reader,HISTORY_TYPE);
            if(saved!=null)for(CoordinateEntry entry:saved)
                if(entry!=null&&entry.serverKey!=null&&!entry.serverKey.trim().isEmpty()
                        &&finite(entry.x)&&finite(entry.y)&&finite(entry.z))history.add(entry);
            while(history.size()>MAX_HISTORY)history.remove(history.size()-1);
        } catch(Exception e) {
            System.err.println("[AlphaClient] Could not load waypoint coordinate history: "+e.getMessage());
        } finally { if(reader!=null)try{reader.close();}catch(Exception ignored){} }
    }

    private void saveHistory() {
        File file=historyFile(),temporary=new File(file.getParentFile(),"waypoint-coordinate-history.json.tmp");
        OutputStreamWriter writer=null;
        try {
            writer=new OutputStreamWriter(new FileOutputStream(temporary),"UTF-8");
            GSON.toJson(history,HISTORY_TYPE,writer);
            writer.close();writer=null;
            WaypointFileUtils.replaceAtomically(temporary,file);
        } catch(Exception e) {
            System.err.println("[AlphaClient] Could not save waypoint coordinate history: "+e.getMessage());
        } finally { if(writer!=null)try{writer.close();}catch(Exception ignored){} }
    }

    private File historyFile() {
        Minecraft mc=Minecraft.getMinecraft();
        File base=new File(mc==null?new File("."):mc.mcDataDir,"config/alphaclient");
        if(!base.exists())base.mkdirs();
        return new File(base,"waypoint-coordinate-history.json");
    }

    private static boolean finite(double value){return !Double.isNaN(value)&&!Double.isInfinite(value);}

    public static final class CoordinateEntry {
        private String serverKey;
        private int dimensionId;
        private double x,y,z;
        private long timestamp;
        public CoordinateEntry(){}
        CoordinateEntry(String serverKey,int dimensionId,double x,double y,double z,long timestamp) {
            this.serverKey=serverKey;this.dimensionId=dimensionId;this.x=x;this.y=y;this.z=z;this.timestamp=timestamp;
        }
        public double getX(){return x;}
        public double getY(){return y;}
        public double getZ(){return z;}
        public long getTimestamp(){return timestamp;}
    }
}
