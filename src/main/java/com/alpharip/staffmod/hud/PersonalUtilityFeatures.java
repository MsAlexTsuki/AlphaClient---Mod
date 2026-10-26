package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Local-only personal play statistics and player notes. Player events are inferred from client-visible entities. */
public final class PersonalUtilityFeatures {
    private static final Map<String,String> nicks=new LinkedHashMap<String,String>(), notes=new LinkedHashMap<String,String>();
    private static final Map<String,Long> seen=new LinkedHashMap<String,Long>();
    private static final Set<String> friends=new LinkedHashSet<String>(), present=new HashSet<String>(), dates=new TreeSet<String>();
    private static final List<Long> sessions=new ArrayList<Long>();
    private static boolean loaded, alarmOn=true, breakOn=true;
    private static int alarmH=0, alarmM=0;
    private static String bedtime="23:00", lastDate="", lastAlarm="", lastReminder="";
    private static long sessionStart, lastMove, pauseEnded, lastReminderAt;
    private static double px,py,pz,walk,swim,fly;
    private static boolean hasPos;
    private static File file(){ Minecraft m=Minecraft.getMinecraft(); return new File(new File(m==null?new File("."):m.mcDataDir,"config"),"alphaclient-personal-utilities.json"); }
    private static void load(){
        if(loaded)return; loaded=true; File f=file(); if(!f.isFile())return; Reader r=null;
        try{
            r=new InputStreamReader(new FileInputStream(f),"UTF-8"); JsonObject o=new JsonParser().parse(r).getAsJsonObject();
            readMap(o,"nicknames",nicks); readMap(o,"notes",notes);
            if(o.has("seen"))for(Map.Entry<String,JsonElement>e:o.getAsJsonObject("seen").entrySet())seen.put(e.getKey(),e.getValue().getAsLong());
            if(o.has("friends"))for(JsonElement e:o.getAsJsonArray("friends"))friends.add(e.getAsString());
            if(o.has("dates"))for(JsonElement e:o.getAsJsonArray("dates"))dates.add(e.getAsString());
            if(o.has("sessions"))for(JsonElement e:o.getAsJsonArray("sessions"))sessions.add(e.getAsLong());
            if(o.has("alarmH"))alarmH=o.get("alarmH").getAsInt(); if(o.has("alarmM"))alarmM=o.get("alarmM").getAsInt();
            if(o.has("alarmOn"))alarmOn=o.get("alarmOn").getAsBoolean(); if(o.has("breakOn"))breakOn=o.get("breakOn").getAsBoolean();
            if(o.has("bedtime"))bedtime=o.get("bedtime").getAsString();
        }catch(Exception e){System.out.println("[AlphaClient] Personal utility load failed: "+e.getMessage());}
        finally{if(r!=null)try{r.close();}catch(Exception ignored){}}
    }
    private static void readMap(JsonObject o,String key,Map<String,String> out){if(o.has(key)&&o.get(key).isJsonObject())for(Map.Entry<String,JsonElement>e:o.getAsJsonObject(key).entrySet())out.put(e.getKey(),e.getValue().getAsString());}
    private static void save(){
        JsonObject o=new JsonObject(), a=new JsonObject(), b=new JsonObject(), s=new JsonObject();
        for(Map.Entry<String,String>e:nicks.entrySet())a.addProperty(e.getKey(),e.getValue()); for(Map.Entry<String,String>e:notes.entrySet())b.addProperty(e.getKey(),e.getValue());
        for(Map.Entry<String,Long>e:seen.entrySet())s.addProperty(e.getKey(),e.getValue()); o.add("nicknames",a);o.add("notes",b);o.add("seen",s);
        JsonArray fa=new JsonArray(),da=new JsonArray(),sa=new JsonArray();for(String x:friends)fa.add(x);for(String x:dates)da.add(x);for(Long x:sessions)sa.add(x);
        o.add("friends",fa);o.add("dates",da);o.add("sessions",sa);o.addProperty("alarmH",alarmH);o.addProperty("alarmM",alarmM);o.addProperty("alarmOn",alarmOn);o.addProperty("breakOn",breakOn);o.addProperty("bedtime",bedtime);
        File f=file();if(f.getParentFile()!=null&&!f.getParentFile().exists())f.getParentFile().mkdirs();Writer w=null;
        try{w=new OutputStreamWriter(new FileOutputStream(f),"UTF-8");new GsonBuilder().setPrettyPrinting().create().toJson(o,w);}catch(Exception e){System.out.println("[AlphaClient] Personal utility save failed: "+e.getMessage());}finally{if(w!=null)try{w.close();}catch(Exception ignored){}}
    }
    private static boolean on(String id){load();AlphaModule m=AlphaModuleRegistry.find(id);return m!=null&&m.isEnabled();}
    private static String now(String fmt){return new SimpleDateFormat(fmt,Locale.ROOT).format(new Date());}
    private static int[] time(String v){try{if(v==null||!v.matches("\\d{1,2}:\\d{2}"))return null;String[]p=v.split(":");int h=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]);return h<0||h>23||m<0||m>59?null:new int[]{h,m};}catch(Exception e){return null;}}
    private static String duration(long sec){sec=Math.max(0,sec);long h=sec/3600,m=(sec%3600)/60,s=sec%60;return h>0?String.format(Locale.ROOT,"%d:%02d:%02d",h,m,s):String.format(Locale.ROOT,"%02d:%02d",m,s);}
    private static void tell(String s){Minecraft m=Minecraft.getMinecraft();if(m!=null&&m.thePlayer!=null)m.thePlayer.addChatMessage(new ChatComponentText("§6[AlphaClient] §f"+s));}
    private static String join(String[]a,int start){StringBuilder b=new StringBuilder();for(int i=start;i<a.length;i++){if(i>start)b.append(' ');b.append(a[i]);}return b.toString();}
    private static long average(){if(sessions.isEmpty())return 0;long n=0;for(Long x:sessions)n+=x;return n/sessions.size();}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;load();Minecraft m=Minecraft.getMinecraft();long t=System.currentTimeMillis();
        if(m==null||m.thePlayer==null||m.theWorld==null){endSession(t);hasPos=false;present.clear();return;}
        if(sessionStart==0) {sessionStart=t;lastMove=t;}
        String day=now("yyyy-MM-dd");if(!day.equals(lastDate)){lastDate=day;if(on("annual-play-calendar")){dates.add(day);save();}}
        double x=m.thePlayer.posX,y=m.thePlayer.posY,z=m.thePlayer.posZ;
        if(!hasPos){px=x;py=y;pz=z;hasPos=true;}else{double dx=x-px,dy=y-py,dz=z-pz,d=Math.sqrt(dx*dx+dy*dy+dz*dz);
            if(d>0&&d<10){if(m.thePlayer.capabilities.isFlying)fly+=d;else if(m.thePlayer.isInWater())swim+=d;else if(m.thePlayer.onGround)walk+=Math.sqrt(dx*dx+dz*dz);}
            if(d>0.02){if(lastMove>0&&t-lastMove>300000L)pauseEnded=t;lastMove=t;}px=x;py=y;pz=z;}
        if(on("late-sleep-reminder")){int[] bt=time(bedtime);if(bt!=null){Calendar c=Calendar.getInstance();String d=now("yyyy-MM-dd");if((c.get(Calendar.HOUR_OF_DAY)>bt[0]||(c.get(Calendar.HOUR_OF_DAY)==bt[0]&&c.get(Calendar.MINUTE)>=bt[1]))&&(!d.equals(lastReminder)||t-lastReminderAt>3600000L)){lastReminder=d;lastReminderAt=t;tell("It is late ("+bedtime+"). Consider taking a break and getting some sleep.");}}}
        if(on("real-clock-alarm")&&alarmOn){Calendar c=Calendar.getInstance();String key=now("yyyy-MM-dd HH:mm");if(c.get(Calendar.HOUR_OF_DAY)==alarmH&&c.get(Calendar.MINUTE)==alarmM&&!key.equals(lastAlarm)){lastAlarm=key;tell("Alarm: "+String.format(Locale.ROOT,"%02d:%02d",alarmH,alarmM));m.thePlayer.playSound("random.orb",1F,1F);}}
        Set<String> cur=new HashSet<String>();for(Object obj:m.theWorld.playerEntities)if(obj instanceof EntityPlayer&&obj!=m.thePlayer){EntityPlayer p=(EntityPlayer)obj;String k=p.getName().toLowerCase(Locale.ROOT);cur.add(k);seen.put(k,t);if(!present.contains(k)&&friends.contains(k)&&on("friend-join-leave"))tell("Friend joined: "+nicks.containsKey(k)?nicks.get(k):p.getName());}
        for(String k:present)if(!cur.contains(k)){seen.put(k,t);if(friends.contains(k)&&on("friend-join-leave"))tell("Friend left: "+(nicks.containsKey(k)?nicks.get(k):k));}
        if(!cur.equals(present))save();present.clear();present.addAll(cur);
    }
    private static void endSession(long t){if(sessionStart>0){long sec=(t-sessionStart)/1000;if(sec>=60&&on("session-average-comparison")){sessions.add(sec);while(sessions.size()>60)sessions.remove(0);save();}sessionStart=0;}}
    @SubscribeEvent public void render(RenderGameOverlayEvent.Text e){
        if(e.type!=RenderGameOverlayEvent.ElementType.TEXT)return;Minecraft m=Minecraft.getMinecraft();if(m==null||m.thePlayer==null||m.theWorld==null||m.fontRendererObj==null||m.gameSettings.hideGUI||m.gameSettings.showDebugInfo)return;load();int row=0;
        if(on("break-since-last-pause"))draw(m,"Since last pause: "+(pauseEnded==0?"no pause recorded":duration((System.currentTimeMillis()-pauseEnded)/1000)),row++);
        if(on("late-sleep-reminder"))draw(m,"Local time: "+now("HH:mm"),row++);
        if(on("real-clock-alarm"))draw(m,"Clock "+now("HH:mm:ss")+(alarmOn?" | Alarm "+String.format(Locale.ROOT,"%02d:%02d",alarmH,alarmM):""),row++);
        if(on("movement-distance-total"))draw(m,String.format(Locale.ROOT,"Distance W %.0fm S %.0fm F %.0fm",walk,swim,fly),row++);
        if(on("session-average-comparison")&&sessionStart>0){long s=(System.currentTimeMillis()-sessionStart)/1000,a=average();draw(m,"Session "+duration(s)+" | Avg "+(sessions.isEmpty()?"not set":duration(a))+(sessions.isEmpty()?"":s>=a?" (+"+duration(s-a)+")":" (-"+duration(a-s)+")"),row++);}
        if(on("annual-play-calendar")){String yr=now("yyyy");int count=0;for(String d:dates)if(d.startsWith(yr+"-"))count++;draw(m,"Play calendar "+yr+": "+count+" days",row++);}
        if(m.objectMouseOver!=null&&m.objectMouseOver.entityHit instanceof EntityPlayer){EntityPlayer p=(EntityPlayer)m.objectMouseOver.entityHit;String k=p.getName().toLowerCase(Locale.ROOT);if(on("player-nicknames")&&nicks.containsKey(k))draw(m,"Nickname: "+nicks.get(k),row++);if(on("player-notes")&&notes.containsKey(k))draw(m,"Note: "+notes.get(k),row++);}
    }
    private static void draw(Minecraft m,String s,int row){ScaledResolution r=new ScaledResolution(m);m.fontRendererObj.drawStringWithShadow(s,Math.max(2,r.getScaledWidth()-m.fontRendererObj.getStringWidth(s)-6),6+row*12,0xFFFFFFFF);}
    public static boolean handleCommand(net.minecraft.command.ICommandSender sender,String[] args){
        load();if(args==null||args.length==0||"help".equalsIgnoreCase(args[0])){sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §fPersonal: nickname <player> <name|clear>, note <player> <text|clear>, friend <add|remove|list> [player], seen <player>, clock, alarm <HH:mm|off>, sleep <HH:mm>, break <on|off>, distance, calendar, sessions"));return true;}
        String a=args[0].toLowerCase(Locale.ROOT);
        if("nickname".equals(a)||"nick".equals(a)){if(args.length<3){tellSender(sender,"Usage: nickname <player> <name|clear>");return true;}String k=args[1].toLowerCase(Locale.ROOT);if("clear".equalsIgnoreCase(args[2]))nicks.remove(k);else nicks.put(k,join(args,2));save();tellSender(sender,"Nickname saved for "+args[1]+".");return true;}
        if("note".equals(a)){if(args.length<3){tellSender(sender,"Usage: note <player> <text|clear>");return true;}String k=args[1].toLowerCase(Locale.ROOT);if("clear".equalsIgnoreCase(args[2]))notes.remove(k);else notes.put(k,join(args,2));save();tellSender(sender,"Local note saved for "+args[1]+".");return true;}
        if("friend".equals(a)){if(args.length==2&&"list".equalsIgnoreCase(args[1])){tellSender(sender,"Friends: "+(friends.isEmpty()?"none":friends.toString()));return true;}if(args.length!=3){tellSender(sender,"Usage: friend <add|remove|list> [player]");return true;}String k=args[2].toLowerCase(Locale.ROOT);if("add".equalsIgnoreCase(args[1]))friends.add(k);else if("remove".equalsIgnoreCase(args[1]))friends.remove(k);else{tellSender(sender,"Usage: friend <add|remove|list> [player]");return true;}save();tellSender(sender,"Friend list updated.");return true;}
        if("seen".equals(a)&&args.length==2){Long t=seen.get(args[1].toLowerCase(Locale.ROOT));tellSender(sender,t==null?"No local last-seen record for "+args[1]+".":args[1]+" last seen "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ROOT).format(new Date(t))+".");return true;}
        if("clock".equals(a)){tellSender(sender,"Local time "+now("yyyy-MM-dd HH:mm:ss z")+".");return true;}
        if("alarm".equals(a)&&args.length==2){if("off".equalsIgnoreCase(args[1]))alarmOn=false;else{int[]t=time(args[1]);if(t==null){tellSender(sender,"Use alarm HH:mm or alarm off.");return true;}alarmH=t[0];alarmM=t[1];alarmOn=true;}save();tellSender(sender,"Alarm "+(alarmOn?String.format(Locale.ROOT,"set to %02d:%02d",alarmH,alarmM):"off")+".");return true;}
        if("sleep".equals(a)&&args.length==2){int[]t=time(args[1]);if(t==null){tellSender(sender,"Use sleep HH:mm.");return true;}bedtime=String.format(Locale.ROOT,"%02d:%02d",t[0],t[1]);save();tellSender(sender,"Sleep reminder set for "+bedtime+".");return true;}
        if("break".equals(a)&&args.length==2){breakOn="on".equalsIgnoreCase(args[1]);save();tellSender(sender,"Break timer "+(breakOn?"on":"off")+".");return true;}
        if("distance".equals(a)){tellSender(sender,String.format(Locale.ROOT,"Walked %.1fm, swum %.1fm, flown %.1fm.",walk,swim,fly));return true;}
        if("calendar".equals(a)){String yr=now("yyyy");int c=0;for(String d:dates)if(d.startsWith(yr+"-"))c++;tellSender(sender,yr+" play days: "+c);return true;}
        if("sessions".equals(a)){tellSender(sender,sessions.isEmpty()?"No completed sessions yet.":"Sessions "+sessions.size()+", average "+duration(average())+".");return true;}
        tellSender(sender,"Unknown personal command. Use /alphaclient personal help.");return true;
    }
    private static void tellSender(net.minecraft.command.ICommandSender s,String msg){s.addChatMessage(new ChatComponentText("§6[AlphaClient] §f"+msg));}
}
