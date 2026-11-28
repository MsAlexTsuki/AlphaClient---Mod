package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Detects leadership roles without a named successor. */
public final class SuccessionGapAlert {
 public static final String NAME="Succession Gap Alert",CATEGORY="OwnerStaffing",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,String> successors=new LinkedHashMap<String,String>();
 public synchronized void assign(String role,String successor){successors.put(req(role),successor==null?"":successor.trim());}
 public synchronized void removeRole(String role){successors.remove(req(role));}
 public synchronized List<String> gaps(){List<String> out=new ArrayList<String>();for(Map.Entry<String,String> e:successors.entrySet())if(e.getValue().isEmpty())out.add(e.getKey());return Collections.unmodifiableList(out);}
 public synchronized boolean hasGap(){return !gaps().isEmpty();}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("role required");return s.trim();}
}