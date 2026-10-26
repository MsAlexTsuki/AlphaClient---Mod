package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Tracks a linear beginner quest chain in memory. */
public final class StarterQuestChain {
 public static final String FEATURE_NAME="Starter Quest Chain",CATEGORY="Donator Scout",STATUS="NEW";
 private final List<String> quests=new ArrayList<String>(); private final Set<Integer> completed=new HashSet<Integer>();
 public void addQuest(String title){if(title==null||title.trim().isEmpty())throw new IllegalArgumentException("title required");quests.add(title.trim());}
 public boolean complete(int index){if(index<0||index>=quests.size())return false;if(index>0&&!completed.contains(index-1))return false;completed.add(index);return true;}
 public List<String> getQuests(){return Collections.unmodifiableList(new ArrayList<String>(quests));}
 public int getCompletedCount(){return completed.size();}
 public boolean isComplete(){return !quests.isEmpty()&&completed.size()==quests.size();}
}