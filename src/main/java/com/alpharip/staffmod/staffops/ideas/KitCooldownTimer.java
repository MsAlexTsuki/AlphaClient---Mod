package com.alpharip.staffmod.staffops.ideas;
import java.util.regex.*;
/** Configurable chat-triggered kit cooldown tracker; register a server-specific regex before use. */
public final class KitCooldownTimer {
 public static final String NAME="Kit Cooldown Timer",CATEGORY="KitMap",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private Pattern trigger;private long cooldownMillis,lastStarted;
 public KitCooldownTimer(long cooldownMillis){setCooldownMillis(cooldownMillis);}
 public synchronized void setTriggerRegex(String regex){trigger=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);}
 public synchronized void setCooldownMillis(long ms){if(ms<0)throw new IllegalArgumentException("cooldown must not be negative");cooldownMillis=ms;}
 public synchronized boolean onChat(String message,long now){if(trigger==null||message==null||!trigger.matcher(message).find())return false;lastStarted=now;return true;}
 public synchronized long getRemainingMillis(long now){return Math.max(0L,cooldownMillis-Math.max(0L,now-lastStarted));}
 public synchronized boolean isReady(long now){return lastStarted==0L||getRemainingMillis(now)==0L;}
 public synchronized void reset(){lastStarted=0L;}
}