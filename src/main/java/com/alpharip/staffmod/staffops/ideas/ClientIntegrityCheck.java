package com.alpharip.staffmod.staffops.ideas;

/** Foundation for storing client file integrity-check results. */
public class ClientIntegrityCheck {
 public static final class Result { public final String path,expectedHash,actualHash; public final boolean matches; public Result(String p,String e,String a){path=p;expectedHash=e;actualHash=a;matches=e!=null&&e.equalsIgnoreCase(a==null?"":a);} }
 private final java.util.Map<String,Result> results=new java.util.LinkedHashMap<String,Result>();
 public void record(String path,String expectedHash,String actualHash){if(path!=null&&!path.trim().isEmpty())results.put(path,new Result(path,expectedHash,actualHash));}
 public Result get(String path){return results.get(path);}
 public boolean allMatch(){for(Result r:results.values())if(!r.matches)return false;return !results.isEmpty();}
 public java.util.Map<String,Result> getResults(){return java.util.Collections.unmodifiableMap(results);}
}
