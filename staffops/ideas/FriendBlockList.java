package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Normalized local friend block list. Enforcement in friend/chat systems is not connected. */
public class FriendBlockList {
 private final LinkedHashMap<String,String> blocked=new LinkedHashMap<String,String>();
 private static String key(String name){if(name==null||name.trim().isEmpty())throw new IllegalArgumentException("Player name required");return name.trim().toLowerCase(Locale.ROOT);}
 public void block(String name){String k=key(name);blocked.put(k,name.trim());}public boolean unblock(String name){return blocked.remove(key(name))!=null;}public boolean isBlocked(String name){return name!=null&&blocked.containsKey(name.trim().toLowerCase(Locale.ROOT));}
 public List<String> list(){return Collections.unmodifiableList(new ArrayList<String>(blocked.values()));}public void clear(){blocked.clear();}
}