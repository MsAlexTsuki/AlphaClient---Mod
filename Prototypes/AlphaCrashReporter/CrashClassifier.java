package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.Locale;
public final class CrashClassifier {
 private CrashClassifier(){}
 public static CrashCategory classify(Throwable t,CrashSource source,String stack){
  String type=t==null?"":t.getClass().getName().toLowerCase(Locale.ROOT);
  String s=(type+" "+(stack==null?"":stack)).toLowerCase(Locale.ROOT);
  if(s.contains("outofmemory")||s.contains("java heap space"))return CrashCategory.MEMORY;
  if(s.contains("opengl")||s.contains("lwjgl")||s.contains("render"))return CrashCategory.GRAPHICS;
  if(s.contains("texture")||s.contains("resource"))return CrashCategory.RESOURCE;
  if(s.contains("net.minecraft"))return CrashCategory.MINECRAFT;
  if(s.contains("net.minecraftforge")||s.contains("forge"))return CrashCategory.FORGE;
  if(s.contains("com.alpharip"))return source==CrashSource.ALPHA_MODULE?CrashCategory.ALPHA_MODULE:CrashCategory.ALPHA_CLIENT;
  if(s.contains("socket")||s.contains("network")||s.contains("http"))return CrashCategory.NETWORK;
  if(type.startsWith("java.")||type.startsWith("javax."))return CrashCategory.JAVA_RUNTIME;
  return CrashCategory.UNKNOWN;
 }
}
