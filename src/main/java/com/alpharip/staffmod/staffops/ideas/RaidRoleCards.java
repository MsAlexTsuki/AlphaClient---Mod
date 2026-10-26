package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores raid role assignments for later card rendering by the staff/client UI. */
public final class RaidRoleCards {
 public static final String NAME="Raid Role Cards",CATEGORY="HardcoreFactions",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Role {public final String player,role,notes;private Role(String p,String r,String n){player=p;role=r;notes=n;}}
 private final Map<String,Role> roles=new LinkedHashMap<String,Role>();
 public synchronized void assign(String player,String role,String notes){String p=text(player);roles.put(p.toLowerCase(Locale.ROOT),new Role(p,text(role),notes==null?"":notes.trim()));}
 public synchronized Role get(String player){return roles.get(text(player).toLowerCase(Locale.ROOT));}
 public synchronized List<Role> snapshot(){return Collections.unmodifiableList(new ArrayList<Role>(roles.values()));}
 public synchronized void clear(){roles.clear();}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}