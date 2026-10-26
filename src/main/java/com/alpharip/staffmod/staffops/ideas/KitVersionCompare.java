package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Compares named saved kit snapshots. Inventory capture and storage are handled by integration code. */
public final class KitVersionCompare {
 public static final String NAME="Kit Version Compare",CATEGORY="KitMap",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Difference {public final String slot,left,right;private Difference(String s,String l,String r){slot=s;left=l;right=r;}}
 private final Map<String,Map<String,String>> kits=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void save(String name,Map<String,String> slots){String n=text(name);if(slots==null)throw new IllegalArgumentException("slots required");kits.put(n,Collections.unmodifiableMap(new LinkedHashMap<String,String>(slots)));}
 public synchronized List<Difference> compare(String leftName,String rightName){Map<String,String>a=kits.get(text(leftName)),b=kits.get(text(rightName));if(a==null||b==null)throw new IllegalArgumentException("Both kit versions must exist");Set<String> keys=new TreeSet<String>();keys.addAll(a.keySet());keys.addAll(b.keySet());List<Difference> out=new ArrayList<Difference>();for(String k:keys){String x=a.get(k),y=b.get(k);if(!Objects.equals(x,y))out.add(new Difference(k,x,y));}return Collections.unmodifiableList(out);}
 public synchronized Set<String> names(){return Collections.unmodifiableSet(new LinkedHashSet<String>(kits.keySet()));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("name must not be blank");return s.trim();}
}