package com.alpharip.staffmod.staffops.ideas;

/** Foundation for opt-in, usage-based module suggestions. TODO(Supabase): persist consent and anonymized aggregates if enabled. */
public class UsageBasedModuleRecommendations {
 private boolean enabled; private final java.util.Map<String,Integer> usage=new java.util.LinkedHashMap<String,Integer>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void recordUsage(String module){if(enabled&&module!=null&&!module.trim().isEmpty())usage.put(module,(usage.containsKey(module)?usage.get(module):0)+1);}
 public java.util.Map<String,Integer> getUsage(){return java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<String,Integer>(usage));}
}
