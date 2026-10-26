package com.alpharip.staffmod.staffops.ideas;
import java.time.*;
/** Evaluates local quiet-hour windows; notification delivery/muting is integrated elsewhere. */
public final class StaffQuietHours {
 public static final String NAME="Staff Quiet Hours",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private LocalTime start,end;private ZoneId zone=ZoneId.systemDefault();private boolean enabled;
 public synchronized void configure(LocalTime start,LocalTime end,ZoneId zone){if(start==null||end==null)throw new IllegalArgumentException("times required");this.start=start;this.end=end;if(zone!=null)this.zone=zone;enabled=true;}
 public synchronized void disable(){enabled=false;}public synchronized boolean isQuiet(Instant instant){if(!enabled)return false;LocalTime now=LocalTime.ofInstant(instant,zone);if(start.equals(end))return true;if(start.isBefore(end))return !now.isBefore(start)&&now.isBefore(end);return !now.isBefore(start)||now.isBefore(end);}
}