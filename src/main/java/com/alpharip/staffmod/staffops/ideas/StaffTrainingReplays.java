package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Stores notes and learning points for named staff training scenarios. */
public final class StaffTrainingReplays {
 public static final String FEATURE_NAME="Staff Training Replays",CATEGORY="Cross-Category",STATUS="NEW";
 public static final class ReplayNote {public final String scenario,notes;private ReplayNote(String s,String n){scenario=s;notes=n;}}
 private final List<ReplayNote> notes=new ArrayList<ReplayNote>();
 public void add(String scenario,String note){if(scenario==null||scenario.trim().isEmpty())throw new IllegalArgumentException("scenario required");notes.add(new ReplayNote(scenario.trim(),note==null?"":note.trim()));}
 public List<ReplayNote> getNotes(){return Collections.unmodifiableList(new ArrayList<ReplayNote>(notes));}
}