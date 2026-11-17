package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.*;
/** Transparent heuristic hints; they do not claim to prove a root cause. */
public final class CrashFixCatalog {
 public List<SuggestedFix> suggest(CrashReport r){
  if(r==null)return Collections.emptyList();String s=(r.getExceptionType()+" "+r.getMessage()+" "+r.getStackTrace()).toLowerCase(Locale.ROOT);
  List<SuggestedFix> out=new ArrayList<SuggestedFix>();
  if(s.contains("outofmemory")||s.contains("java heap space"))add(out,"memory","Review allocated memory","Check launcher allocation and close memory-heavy applications. Do not exceed available system memory.",CrashCategory.MEMORY,85);
  if(s.contains("opengl")||s.contains("lwjgl")||s.contains("pixel format"))add(out,"graphics","Check graphics compatibility","Verify graphics drivers and rendering configuration; compare with a clean Forge 1.8.9 launch.",CrashCategory.GRAPHICS,75);
  if(s.contains("nosuchmethod")||s.contains("noclassdeffound"))add(out,"dependency","Check mod dependencies","Look for missing, duplicated or mismatched mod and dependency versions.",CrashCategory.FORGE,80);
  if(s.contains("texture")||s.contains("resource")||s.contains("json"))add(out,"resources","Inspect resource files","Check resource paths, file validity and recent resource-pack or asset changes.",CrashCategory.RESOURCE,60);
  if(s.contains("timeout")||s.contains("socket")||s.contains("unknownhost"))add(out,"network","Check network availability","Verify connectivity. A network symptom alone does not establish a server-side fault.",CrashCategory.NETWORK,65);
  if(out.isEmpty())add(out,"stack","Inspect the first relevant stack frame","Start with the first frame belonging to the failing mod or client component and compare with the last known working build.",r.getCategory(),35);
  return Collections.unmodifiableList(out);
 }
 private static void add(List<SuggestedFix> l,String id,String title,String desc,CrashCategory c,int confidence){l.add(new SuggestedFix(id,title,desc,c,confidence));}
}
