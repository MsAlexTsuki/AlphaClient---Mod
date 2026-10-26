package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking achievements shown on a profile wall. */
public class AchievementWall {
 private final java.util.Map<String,Long> unlocked=new java.util.LinkedHashMap<String,Long>();
 public void unlock(String achievementId,long timestampMillis){if(achievementId!=null&&!achievementId.trim().isEmpty()&&!unlocked.containsKey(achievementId))unlocked.put(achievementId,timestampMillis);}
 public boolean isUnlocked(String achievementId){return unlocked.containsKey(achievementId);}
 public Long getUnlockTime(String achievementId){return unlocked.get(achievementId);}
 public java.util.Map<String,Long> getUnlocked(){return java.util.Collections.unmodifiableMap(unlocked);}
}
