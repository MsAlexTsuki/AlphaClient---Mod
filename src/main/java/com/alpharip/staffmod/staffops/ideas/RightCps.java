package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Right CPS. Tracks right-click samples over a configurable rolling time window.
 * Client-side foundation only; Minecraft event hooks, rendering and network integration are separate work.
 */
public final class RightCps {
 public static final String FEATURE_NAME="Right CPS";
 public static final String DESCRIPTION="Tracks right-click samples over a configurable rolling time window.";
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
 private final List<Long> clicks=new ArrayList<Long>(); public void recordClick(long now){clicks.add(now);} public int count(long now,long window){while(!clicks.isEmpty()&&now-clicks.get(0)>window)clicks.remove(0);return clicks.size();}
}
