package com.alpharip.staffmod.staffops.ideas;

/** Foundation for displaying the message of the day when joining a server. */
public class ServerMotdOnJoin {
 private boolean enabled=true,shownForCurrentJoin; private String motd="";
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getMotd(){return motd;} public void setMotd(String v){motd=v==null?"":v;}
 public boolean shouldShow(){return enabled&&!shownForCurrentJoin&&!motd.trim().isEmpty();}
 public void markShown(){shownForCurrentJoin=true;} public void onJoin(){shownForCurrentJoin=false;}
}
