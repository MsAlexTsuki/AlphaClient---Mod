package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Streak Counter with Bonus XP. Calculates streak length and bonus XP from consecutive completion days.
 * Client-side foundation only; Minecraft hooks and any required remote services are separate work.
 */
public final class StreakCounterBonusXp {
 public static final String FEATURE_NAME="Streak Counter with Bonus XP";
 public static final String DESCRIPTION="Calculates streak length and bonus XP from consecutive completion days.";
 private boolean enabled=true;
 private final Map<String,String> options=new LinkedHashMap<String,String>();
 private final List<String> records=new ArrayList<String>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean value){enabled=value;}
 public void setOption(String key,String value){if(key==null||key.trim().isEmpty())return;if(value==null)options.remove(key);else options.put(key,value);}
 public String getOption(String key,String fallback){String value=options.get(key);return value==null?fallback:value;}
 public Map<String,String> getOptions(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(options));}
 public void record(String value){if(value!=null)records.add(value);}
 public List<String> getRecords(){return Collections.unmodifiableList(new ArrayList<String>(records));}
 public List<String> searchRecords(String query){List<String> out=new ArrayList<String>();if(query==null||query.trim().isEmpty())return getRecords();String q=query.toLowerCase(Locale.ROOT);for(String v:records)if(v.toLowerCase(Locale.ROOT).contains(q))out.add(v);return out;}
 public void clearRecords(){records.clear();}
 private int streak; private String lastDay=""; public int completeDay(String day){if(day==null||day.equals(lastDay))return streak;String prev=getOption("previousDayKey","");streak=prev.isEmpty()?1:(prev.equals(day)?streak:streak+1);lastDay=day;setOption("previousDayKey",day);return streak;} public int bonusXp(int perDay,int cap){return Math.min(Math.max(0,cap),Math.max(0,streak*perDay));}
}
