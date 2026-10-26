package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking player-count and ping samples after joining a server. */
public class PlayerCountAndPingTrendOnJoin {
 private final java.util.List<Integer> playerCounts=new java.util.ArrayList<Integer>(); private final java.util.List<Integer> pings=new java.util.ArrayList<Integer>();
 public void record(int players,int pingMillis){playerCounts.add(Math.max(0,players));pings.add(Math.max(0,pingMillis));}
 public java.util.List<Integer> getPlayerCounts(){return java.util.Collections.unmodifiableList(playerCounts);}
 public java.util.List<Integer> getPings(){return java.util.Collections.unmodifiableList(pings);}
 public void clear(){playerCounts.clear();pings.clear();}
}
