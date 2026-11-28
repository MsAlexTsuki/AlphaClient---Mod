package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks open handover items and their status. */
public final class HandoverPendingList {
 public static final String NAME="Handover Pending List",CATEGORY="StaffMods",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 public static final class Item {public final String id,description,owner;public final long createdAt;private boolean resolved;private Item(String i,String d,String o){id=i;description=d;owner=o;createdAt=System.currentTimeMillis();}public boolean isResolved(){return resolved;}}
 private final Map<String,Item> items=new LinkedHashMap<String,Item>();
 public synchronized void add(String id,String description,String owner){String i=req(id);items.put(i,new Item(i,req(description),owner==null?"":owner.trim()));}
 public synchronized void resolve(String id,boolean resolved){Item i=items.get(req(id));if(i==null)throw new IllegalArgumentException("Unknown handover item");i.resolved=resolved;}
 public synchronized List<Item> pending(){List<Item> out=new ArrayList<Item>();for(Item i:items.values())if(!i.resolved)out.add(i);return Collections.unmodifiableList(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}