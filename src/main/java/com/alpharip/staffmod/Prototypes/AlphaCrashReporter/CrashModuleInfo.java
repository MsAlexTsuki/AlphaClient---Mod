package com.alpharip.staffmod.AlphaCrashReporter;
/** Optional module metadata supplied by a future integration adapter. */
public final class CrashModuleInfo {
 private final String id,name,version;
 public CrashModuleInfo(String id,String name,String version){this.id=clean(id);this.name=clean(name);this.version=clean(version);}
 private static String clean(String s){return s==null?"":s.trim();}
 public String getId(){return id;} public String getDisplayName(){return name;} public String getVersion(){return version;}
}
