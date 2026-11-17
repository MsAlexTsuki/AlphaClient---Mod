package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.*;
/** Deliberately excludes account identifiers and credentials. */
public final class CrashEnvironment {
 private final Map<String,String> values;
 public CrashEnvironment(Map<String,String> v){values=new LinkedHashMap<String,String>();if(v!=null)values.putAll(v);}
 public static CrashEnvironment capture(){
  Map<String,String> v=new LinkedHashMap<String,String>();
  String[] keys={"java.version","java.vendor","os.name","os.version","os.arch"};
  for(String k:keys)v.put(k,System.getProperty(k,"unknown"));
  Runtime r=Runtime.getRuntime();v.put("available.processors",String.valueOf(r.availableProcessors()));
  v.put("memory.max",String.valueOf(r.maxMemory()));v.put("memory.total",String.valueOf(r.totalMemory()));v.put("memory.free",String.valueOf(r.freeMemory()));
  return new CrashEnvironment(v);
 }
 public Map<String,String> getValues(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(values));}
}
