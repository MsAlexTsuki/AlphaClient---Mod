package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Holds a read-only preview list of perks for a named rank; does not grant permissions. */
public final class RankPerkPreview {
 public static final String FEATURE_NAME="Rank Perk Preview",CATEGORY="Cross-Category",STATUS="NEW";
 private String rank; private final List<String> perks=new ArrayList<String>();
 public void setRank(String value){if(value==null||value.trim().isEmpty())throw new IllegalArgumentException("rank required");rank=value.trim();}
 public String getRank(){return rank;} public void addPerk(String value){if(value!=null&&!value.trim().isEmpty())perks.add(value.trim());}
 public List<String> getPerks(){return Collections.unmodifiableList(new ArrayList<String>(perks));}
}