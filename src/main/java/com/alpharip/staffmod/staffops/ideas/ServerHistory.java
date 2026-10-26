package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Server History. Maintains a bounded most-recently-used server connection history.
 * Client-side foundation only; Minecraft event hooks, rendering and network integration are separate work.
 */
public final class ServerHistory {
 public static final String FEATURE_NAME="Server History";
 public static final String DESCRIPTION="Maintains a bounded most-recently-used server connection history.";
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
 private final List<String> history=new ArrayList<String>(); public void record(String address,int limit){if(address==null||address.trim().isEmpty())return;history.remove(address);history.add(0,address);while(history.size()>Math.max(1,limit))history.remove(history.size()-1);} public List<String> getHistory(){return Collections.unmodifiableList(new ArrayList<String>(history));}
}
