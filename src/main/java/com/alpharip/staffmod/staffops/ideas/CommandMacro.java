package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores named command macros; executing them requires explicit UI action and server safeguards. */
public class CommandMacro {private final Map<String,List<String>> macros=new LinkedHashMap<String,List<String>>();
 public void put(String name,List<String> commands){String n=req(name);if(commands==null||commands.isEmpty())throw new IllegalArgumentException();List<String> c=new ArrayList<String>();for(String s:commands){String v=req(s);if(!v.startsWith("/"))throw new IllegalArgumentException("Commands must start with /");c.add(v);}macros.put(n,c);}
 public List<String> get(String name){List<String> v=macros.get(req(name));return v==null?Collections.<String>emptyList():Collections.unmodifiableList(new ArrayList<String>(v));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}