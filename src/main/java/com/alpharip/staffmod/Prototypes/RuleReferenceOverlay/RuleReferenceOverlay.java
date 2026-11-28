package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
/** Standalone prototype facade. Keeps the original enabled flag and record API. */
public final class RuleReferenceOverlay {
 public static final String NAME="Rule Reference Overlay",CATEGORY="Trial Moderator",STATUS="NEW",PURPOSE="Show relevant rules while handling a report.",WORKFLOW="Context-sensitive rule cards for chat, combat, or build reports.";
 public static final boolean REQUIRES_SUPABASE=false,REQUIRES_SERVER_SUPPORT=false;
 private boolean enabled=true;private final List<String> records=new ArrayList<String>();
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public synchronized void addRecord(String record){if(record!=null&&!record.trim().isEmpty())records.add(record.trim());}
 public synchronized List<String> getRecords(){return Collections.unmodifiableList(new ArrayList<String>(records));}
 public synchronized void clearRecords(){records.clear();}
}