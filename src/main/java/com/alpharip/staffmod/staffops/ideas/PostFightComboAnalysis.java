package com.alpharip.staffmod.staffops.ideas;

/** Foundation for storing post-fight combo metrics. */
public class PostFightComboAnalysis {
 private int hits, longestCombo; private long fightDurationMillis;
 public void recordFight(int hitCount,int combo,long duration){hits=Math.max(0,hitCount);longestCombo=Math.max(0,combo);fightDurationMillis=Math.max(0L,duration);}
 public int getHits(){return hits;} public int getLongestCombo(){return longestCombo;} public long getFightDurationMillis(){return fightDurationMillis;}
}
