package com.alpharip.staffmod.staffops.ideas;

/** Foundation for maintaining nearby player distances for a HUD list. */
public class NearbyPlayerDistanceList {
 private final java.util.Map<String,Double> distances=new java.util.LinkedHashMap<String,Double>(); private double maxDistance=64.0;
 public void update(String player,double distance){if(player!=null&&distance>=0&&distance<=maxDistance)distances.put(player,distance);else if(player!=null)distances.remove(player);}
 public void remove(String player){distances.remove(player);}
 public double getMaxDistance(){return maxDistance;} public void setMaxDistance(double v){maxDistance=Math.max(1.0,Math.min(512.0,v));}
 public java.util.Map<String,Double> getDistances(){return java.util.Collections.unmodifiableMap(distances);}
}
