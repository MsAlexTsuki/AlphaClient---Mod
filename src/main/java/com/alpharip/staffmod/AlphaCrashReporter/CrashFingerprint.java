package com.alpharip.staffmod.AlphaCrashReporter;
import java.nio.charset.Charset;import java.security.MessageDigest;
/** Stable hash based on exception type, message and first useful stack frames. */
public final class CrashFingerprint {
 private CrashFingerprint(){}
 public static String create(String type,String message,String stack){
  StringBuilder b=new StringBuilder(v(type)).append('|').append(v(message));
  if(stack!=null){int n=0;for(String line:stack.split("\\r?\\n")){String s=line.trim();if(s.startsWith("at ")){b.append('|').append(s.replaceAll(":\\d+\\)", ":#)"));if(++n==5)break;}}}
  try{byte[] d=MessageDigest.getInstance("SHA-256").digest(b.toString().getBytes(Charset.forName("UTF-8")));
   StringBuilder h=new StringBuilder();for(byte x:d)h.append(String.format("%02x",x&255));return h.toString();
  }catch(Exception e){return Integer.toHexString(b.toString().hashCode());}
 }
 private static String v(String s){return s==null?"":s.trim();}
}
