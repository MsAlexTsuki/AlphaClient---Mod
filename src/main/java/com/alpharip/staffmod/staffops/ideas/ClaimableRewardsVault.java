package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Claimable Rewards Vault. Maintains a list of earned rewards awaiting local claim confirmation.
 * Client-side foundation only; Minecraft hooks and any required remote services are separate work.
 */
public final class ClaimableRewardsVault {
 public static final String FEATURE_NAME="Claimable Rewards Vault";
 public static final String DESCRIPTION="Maintains a list of earned rewards awaiting local claim confirmation.";
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
 private final Set<String> earned=new LinkedHashSet<String>(),claimed=new LinkedHashSet<String>(); public void earn(String id){if(id!=null&&!id.trim().isEmpty())earned.add(id);} public boolean claim(String id){return earned.contains(id)&&claimed.add(id);} public Set<String> getClaimable(){Set<String> out=new LinkedHashSet<String>(earned);out.removeAll(claimed);return Collections.unmodifiableSet(out);}
}
