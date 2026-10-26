package com.alpharip.staffmod.staffops.ideas;

/** Foundation for read-only Twitch chat display preferences. */
public class ReadOnlyTwitchChatInGame {
 private boolean enabled; private String channel=""; private int maxMessages=10;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getChannel(){return channel;} public void setChannel(String v){channel=v==null?"":v.trim();}
 public int getMaxMessages(){return maxMessages;} public void setMaxMessages(int v){maxMessages=Math.max(1,Math.min(100,v));}
}
