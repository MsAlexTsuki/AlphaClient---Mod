package com.alpharip.staffmod.staffops.ideas;

import java.time.Duration;
import java.time.Instant;

/** Formats a session summary from timestamps and supplied notes. */
public final class SessionWrapUpReport {
 public static final String FEATURE_NAME="Session Wrap-Up Report",CATEGORY="Cross-Category",STATUS="NEW";
 public String create(Instant start,Instant end,String notes){
  if(start==null||end==null)throw new IllegalArgumentException("start and end required");
  if(end.isBefore(start))throw new IllegalArgumentException("end cannot precede start");
  long minutes=Duration.between(start,end).toMinutes();
  return "Session duration: "+minutes+" minutes. "+(notes==null?"":notes.trim());
 }
}