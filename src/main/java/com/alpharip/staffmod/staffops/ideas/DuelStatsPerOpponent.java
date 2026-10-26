package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Aggregates duel results per opponent. */
public class DuelStatsPerOpponent {
 public static final class Stats {public final int wins,losses;public final long durationMillis;private Stats(int w,int l,long d){wins=w;losses=l;durationMillis=d;}public int total(){return wins+losses;}public double winRate(){return total()==0?0:(double)wins/total();}}
 private static final class Data {int w,l;long d;}
 private final Map<String,Data> data=new LinkedHashMap<String,Data>();
 public void record(String name,boolean won,long duration){if(duration<0)throw new IllegalArgumentException();Data d=data.get(key(name));if(d==null){d=new Data();data.put(key(name),d);}if(won)d.w++;else d.l++;d.d+=duration;}
 public Stats get(String name){Data d=data.get(key(name));return d==null?new Stats(0,0,0):new Stats(d.w,d.l,d.d);}
 public Map<String,Stats> snapshot(){Map<String,Stats> out=new LinkedHashMap<String,Stats>();for(String k:data.keySet())out.put(k,get(k));return Collections.unmodifiableMap(out);}
 private static String key(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("opponent");return s.trim().toLowerCase(Locale.ROOT);}
}