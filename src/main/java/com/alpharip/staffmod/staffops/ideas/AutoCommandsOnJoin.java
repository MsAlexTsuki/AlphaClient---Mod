package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Server-specific opt-in command lists; caller must execute only after explicit user configuration. */
public class AutoCommandsOnJoin {private final Map<String,List<String>> commands=new LinkedHashMap<String,List<String>>();private boolean enabled;
 public void setEnabled(boolean v){enabled=v;}public boolean isEnabled(){return enabled;}
 public void add(String server,String command){String s=req(server).toLowerCase(Locale.ROOT),c=req(command);if(!c.startsWith("/"))throw new IllegalArgumentException("Command must start with /");if(!commands.containsKey(s))commands.put(s,new ArrayList<String>());commands.get(s).add(c);}
 public List<String> getCommands(String server){List<String> v=commands.get(req(server).toLowerCase(Locale.ROOT));return v==null?Collections.<String>emptyList():Collections.unmodifiableList(new ArrayList<String>(v));}
 public void clear(String server){commands.remove(req(server).toLowerCase(Locale.ROOT));}private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}