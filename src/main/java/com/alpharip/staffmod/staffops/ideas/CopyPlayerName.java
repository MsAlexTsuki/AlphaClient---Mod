package com.alpharip.staffmod.staffops.ideas;
/** Validates a player name for clipboard use; UI/clipboard wiring is not connected. */
public class CopyPlayerName {
 private boolean enabled=true;public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public String prepare(String name){if(!enabled||name==null)return "";String n=name.trim();if(n.length()>16)n=n.substring(0,16);return n.matches("[A-Za-z0-9_]{1,16}")?n:"";}
}