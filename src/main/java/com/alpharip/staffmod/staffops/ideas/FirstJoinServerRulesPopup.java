package com.alpharip.staffmod.staffops.ideas;

/** Foundation for remembering whether server rules have been acknowledged. */
public class FirstJoinServerRulesPopup {
 private final java.util.Set<String> acknowledgedServers=new java.util.LinkedHashSet<String>();
 public boolean shouldShow(String serverId){return serverId!=null&&!acknowledgedServers.contains(serverId);}
 public void acknowledge(String serverId){if(serverId!=null&&!serverId.trim().isEmpty())acknowledgedServers.add(serverId);}
 public void reset(String serverId){acknowledgedServers.remove(serverId);}
}
