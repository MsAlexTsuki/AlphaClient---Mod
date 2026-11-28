package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a HUD driven by parsed server scoreboard lines. */
public class HudFromServerScoreboardData {
 private boolean enabled; private final java.util.List<String> lines=new java.util.ArrayList<String>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void setLines(java.util.List<String> values){lines.clear();if(values!=null)lines.addAll(values);}
 public java.util.List<String> getLines(){return java.util.Collections.unmodifiableList(lines);}
}
