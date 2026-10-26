package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks a rotating watch schedule; signaling is returned as state for a UI/notification hook. */
public final class WatchtowerShiftTimer {
 public static final String NAME="Watchtower Shift Timer",CATEGORY="HardcoreFactions",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final List<String> guards=new ArrayList<String>();private long shiftMillis;private long startedAt;private int current;
 public synchronized void setShiftMillis(long ms){if(ms<1000)throw new IllegalArgumentException("shift must be at least one second");shiftMillis=ms;}
 public synchronized void setGuards(List<String> names){if(names==null||names.isEmpty())throw new IllegalArgumentException("guards required");guards.clear();for(String n:names)guards.add(text(n));current=0;}
 public synchronized void start(long now){if(guards.isEmpty()||shiftMillis<=0)throw new IllegalStateException("Configure guards and shift length first");startedAt=now;}
 public synchronized boolean isShiftDue(long now){return startedAt>0&&shiftMillis>0&&now-startedAt>=shiftMillis;}
 public synchronized String rotate(long now){if(!isShiftDue(now))return null;current=(current+1)%guards.size();startedAt=now;return guards.get(current);}
 public synchronized String getCurrentGuard(){return guards.isEmpty()?null:guards.get(current);}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("guard required");return s.trim();}
}