package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Server Favorites. Maintains a deduplicated ordered list of favorite server addresses.
 * Client-side foundation only; Minecraft event hooks, rendering and network integration are separate work.
 */
public final class ServerFavorites {
 public static final String FEATURE_NAME="Server Favorites";
 public static final String DESCRIPTION="Maintains a deduplicated ordered list of favorite server addresses.";
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
 private final List<String> favorites=new ArrayList<String>(); public boolean add(String address){if(address==null||address.trim().isEmpty()||favorites.contains(address))return false;favorites.add(address);return true;} public boolean remove(String address){return favorites.remove(address);} public List<String> getFavorites(){return Collections.unmodifiableList(new ArrayList<String>(favorites));}
}
