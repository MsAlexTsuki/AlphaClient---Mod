package com.alpharip.staffmod.staffops.ideas;

/** Foundation for estimating remaining tool durability. */
public class ToolDurabilityPrediction {
 private int maxDurability; private int damageTaken;
 public void setMaxDurability(int value){maxDurability=Math.max(0,value);damageTaken=Math.min(damageTaken,maxDurability);} public void recordDamage(int amount){if(amount>0)damageTaken=Math.min(maxDurability,damageTaken+amount);} public int getRemainingDurability(){return Math.max(0,maxDurability-damageTaken);} public double getRemainingFraction(){return maxDurability==0?0.0:(double)getRemainingDurability()/maxDurability;}
}
