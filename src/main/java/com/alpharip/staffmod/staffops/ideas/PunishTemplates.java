package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Reusable punishment text templates with named placeholder substitution. */
public class PunishTemplates {private final Map<String,String> templates=new LinkedHashMap<String,String>();
 public void put(String name,String text){templates.put(req(name),req(text));}
 public String render(String name,Map<String,String> values){String out=templates.get(req(name));if(out==null)return null;if(values!=null)for(Map.Entry<String,String> e:values.entrySet())out=out.replace("{"+e.getKey()+"}",e.getValue()==null?"":e.getValue());return out;}
 public Map<String,String> getAll(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(templates));}private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}