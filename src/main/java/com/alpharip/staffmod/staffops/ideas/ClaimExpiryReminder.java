package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks claim expiry deadlines and returns claims inside a caller-defined reminder window. */
public final class ClaimExpiryReminder {
 public static final String NAME="Claim Expiry Reminder",CATEGORY="HardcoreFactions",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Claim {public final String id;public final long expiresAt;private Claim(String i,long e){id=i;expiresAt=e;}}
 private final Map<String,Claim> claims=new LinkedHashMap<String,Claim>();
 public synchronized void put(String id,long expiresAt){if(expiresAt<0)throw new IllegalArgumentException("expiry must be non-negative");claims.put(text(id),new Claim(text(id),expiresAt));}
 public synchronized List<Claim> due(long now,long reminderWindow){if(reminderWindow<0)throw new IllegalArgumentException("window must be non-negative");List<Claim> out=new ArrayList<Claim>();for(Claim c:claims.values())if(c.expiresAt>=now&&c.expiresAt-now<=reminderWindow)out.add(c);return Collections.unmodifiableList(out);}
 public synchronized boolean remove(String id){return claims.remove(text(id))!=null;}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("id required");return s.trim();}
}