package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Server Ping Tracker. Records ping samples and calculates the latest and average latency.
 * Client-side foundation only; Minecraft event hooks, rendering and network integration are separate work.
 */
public final class ServerPingTracker {
 public static final String FEATURE_NAME="Server Ping Tracker";
 public static final String DESCRIPTION="Records ping samples and calculates the latest and average latency.";
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
 private final List<Long> samples=new ArrayList<Long>(); public void recordPing(long millis,int limit){if(millis<0)return;samples.add(millis);while(samples.size()>Math.max(1,limit))samples.remove(0);} public long getLatest(){return samples.isEmpty()?-1L:samples.get(samples.size()-1);} public long getAverage(){if(samples.isEmpty())return -1L;long total=0L;for(Long n:samples)total+=n;return total/samples.size();}
}
