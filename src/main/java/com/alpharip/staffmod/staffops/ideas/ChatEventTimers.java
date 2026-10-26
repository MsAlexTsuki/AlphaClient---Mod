package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Named event countdowns; chat parsing and HUD notifications are separate integrations. */
public class ChatEventTimers {public static final class Timer {public final String name;public final long endAt;private Timer(String n,long e){name=n;endAt=e;}public long remaining(long now){return Math.max(0,endAt-now);}public boolean expired(long now){return now>=endAt;}}
 private final Map<String,Timer> timers=new LinkedHashMap<String,Timer>();
 public void set(String name,long now,long duration){if(now<0||duration<0)throw new IllegalArgumentException();String n=req(name);timers.put(n.toLowerCase(Locale.ROOT),new Timer(n,now+duration));}
 public Timer get(String name){return timers.get(req(name).toLowerCase(Locale.ROOT));}
 public List<Timer> active(long now){List<Timer> out=new ArrayList<Timer>();for(Timer t:timers.values())if(!t.expired(now))out.add(t);return Collections.unmodifiableList(out);}
 public void remove(String name){timers.remove(req(name).toLowerCase(Locale.ROOT));}private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}