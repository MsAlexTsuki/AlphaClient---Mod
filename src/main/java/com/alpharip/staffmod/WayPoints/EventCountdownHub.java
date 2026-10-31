package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local, server-specific chat patterns that start countdowns and optionally add coordinate waypoints. */
public final class EventCountdownHub {
    public static final String NAME="Event Countdown Hub";
    public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=false;
    private static final EventCountdownHub INSTANCE=new EventCountdownHub();
    public static EventCountdownHub getInstance(){return INSTANCE;}

    private final List<EventPattern> patterns=new ArrayList<EventPattern>();
    private final List<Countdown> active=new ArrayList<Countdown>();
    private String loadedServerKey="";

    private EventCountdownHub(){}

    private String serverKey(){return ServerWaypointIntegration.currentServerKey(Minecraft.getMinecraft());}
    private File patternsFile(String key) {
        File base=new File(Minecraft.getMinecraft().mcDataDir,"config/alphaclient");
        if(!base.exists())base.mkdirs();
        return new File(base,"waypoint_event_patterns_"+Integer.toHexString(key.hashCode())+".properties");
    }

    private synchronized void ensureLoaded() {
        String current=serverKey();
        if(current.equals(loadedServerKey))return;
        patterns.clear();
        loadedServerKey=current;
        Properties p=new Properties(); FileInputStream in=null;
        try {
            File f=patternsFile(current); if(!f.isFile())return;
            in=new FileInputStream(f); p.load(in);
            int count=Integer.parseInt(p.getProperty("count","0"));
            for(int i=0;i<count;i++) {
                String name=p.getProperty("name."+i,"").trim();
                String regex=p.getProperty("regex."+i,"");
                int seconds=Integer.parseInt(p.getProperty("seconds."+i,"0"));
                if(!name.isEmpty()&&!regex.isEmpty()&&seconds>0)
                    patterns.add(new EventPattern(name,Pattern.compile(regex,Pattern.CASE_INSENSITIVE),seconds));
            }
        } catch(Exception e) { System.err.println("[AlphaClient] Could not load waypoint event patterns: "+e.getMessage()); }
        finally { if(in!=null)try{in.close();}catch(Exception ignored){} }
    }

    private synchronized void savePatterns() {
        ensureLoaded();
        Properties p=new Properties(); p.setProperty("count",String.valueOf(patterns.size()));
        for(int i=0;i<patterns.size();i++) {
            EventPattern value=patterns.get(i);
            p.setProperty("name."+i,value.id);
            p.setProperty("regex."+i,value.pattern.pattern());
            p.setProperty("seconds."+i,String.valueOf(value.seconds));
        }
        FileOutputStream out=null;
        try { out=new FileOutputStream(patternsFile(loadedServerKey)); p.store(out,"AlphaClient local event patterns"); }
        catch(Exception e) { System.err.println("[AlphaClient] Could not save waypoint event patterns: "+e.getMessage()); }
        finally { if(out!=null)try{out.close();}catch(Exception ignored){} }
    }

    public synchronized void addPattern(String id,String regex,int seconds) {
        ensureLoaded();
        if(id==null||id.trim().isEmpty()||regex==null||regex.isEmpty()||seconds<1)
            throw new IllegalArgumentException("Pattern needs a name, regex and positive duration.");
        Pattern compiled=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        removePattern(id);
        patterns.add(new EventPattern(id.trim(),compiled,seconds));
        savePatterns();
    }

    public synchronized boolean removePattern(String id) {
        ensureLoaded();
        if(id==null)return false;
        for(int i=0;i<patterns.size();i++)if(patterns.get(i).id.equalsIgnoreCase(id.trim())) {
            patterns.remove(i); savePatterns(); return true;
        }
        return false;
    }

    public synchronized List<String> getPatternNames() {
        ensureLoaded();
        List<String> names=new ArrayList<String>();
        for(EventPattern p:patterns)names.add(p.id);
        return Collections.unmodifiableList(names);
    }

    public synchronized List<String> getActiveCountdowns() {
        List<String> values=new ArrayList<String>();
        long now=System.currentTimeMillis();
        String current=serverKey();
        for(Countdown c:active)if(c.endAt>now&&current.equals(c.serverKey))
            values.add(c.name+" "+format((c.endAt-now)/1000L));
        return Collections.unmodifiableList(values);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(event==null||event.message==null||event.type==2)return;
        String message=event.message.getUnformattedText();
        synchronized(this) {
            ensureLoaded();
            for(EventPattern p:patterns) {
                Matcher matcher=p.pattern.matcher(message);
                if(!matcher.find())continue;
                String server=serverKey();
                active.add(new Countdown(p.id,System.currentTimeMillis()+p.seconds*1000L,server));
                if(matcher.groupCount()>=3) {
                    try {
                        double x=Double.parseDouble(matcher.group(1));
                        double y=Double.parseDouble(matcher.group(2));
                        double z=Double.parseDouble(matcher.group(3));
                        StaffWaypoint point=WaypointManager.getInstance().add(p.id,x,y,z);
                        point.setCategory("Event");
                        point.setColor(WaypointCategoriesColors.colorFor("Event"));
                        WaypointManager.getInstance().saveChanges();
                    } catch(Exception ignored) { }
                }
            }
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if(event.type!=RenderGameOverlayEvent.ElementType.TEXT)return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.hideGUI||mc.gameSettings.showDebugInfo)return;
        List<String> lines=getActiveCountdowns(); int y=6;
        for(String line:lines) {
            mc.fontRendererObj.drawStringWithShadow("EVENT  "+line,6,y,0xFF55FFFF);
            y+=mc.fontRendererObj.FONT_HEIGHT+2;
        }
        synchronized(this) {
            long now=System.currentTimeMillis();
            for(int i=active.size()-1;i>=0;i--)if(active.get(i).endAt<=now)active.remove(i);
        }
    }

    private static String format(long sec){return String.format("%02d:%02d",sec/60L,sec%60L);}
    private static final class EventPattern {
        final String id; final Pattern pattern; final int seconds;
        EventPattern(String id,Pattern pattern,int seconds){this.id=id;this.pattern=pattern;this.seconds=seconds;}
    }
    private static final class Countdown {
        final String name; final long endAt; final String serverKey;
        Countdown(String name,long endAt,String serverKey){this.name=name;this.endAt=endAt;this.serverKey=serverKey;}
    }
}
