package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Resolves exact or suffix server-address matches to profile names; profile application is separate. */
public class AutoLoadingServerProfiles {private final Map<String,String> profiles=new LinkedHashMap<String,String>();
 public void bind(String address,String profile){profiles.put(norm(address),req(profile));}
 public String resolve(String address){String key=norm(address),best=null;int len=-1;for(Map.Entry<String,String> e:profiles.entrySet())if((key.equals(e.getKey())||key.endsWith("."+e.getKey()))&&e.getKey().length()>len){best=e.getValue();len=e.getKey().length();}return best;}
 public void remove(String address){profiles.remove(norm(address));}public Map<String,String> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(profiles));}
 private static String norm(String s){return req(s).toLowerCase(Locale.ROOT).replaceAll("^https?://","").replaceAll("/+$","");}private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}