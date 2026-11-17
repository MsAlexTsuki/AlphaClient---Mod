package com.alpharip.staffmod.AlphaCrashReporter;
import java.nio.charset.Charset;import java.util.*;
/** Versioned Base64 line format keeps stack traces and newlines intact. */
public final class CrashReportCodec {
 private static final Charset UTF8=Charset.forName("UTF-8");
 public String encode(CrashReport r){
  StringBuilder b=new StringBuilder("ALPHA_CRASH_REPORT\\t1\\n");
  put(b,"id",r.getId());put(b,"time",String.valueOf(r.getTimestamp()));put(b,"title",r.getTitle());put(b,"type",r.getExceptionType());
  put(b,"message",r.getMessage());put(b,"stack",r.getStackTrace());put(b,"source",r.getSource().name());put(b,"category",r.getCategory().name());
  put(b,"severity",r.getSeverity().name());put(b,"module",r.getModuleName());put(b,"fingerprint",r.getFingerprint());
  put(b,"client",r.getClientVersion());put(b,"minecraft",r.getMinecraftVersion());put(b,"forge",r.getForgeVersion());put(b,"status",r.getStatus().name());
  for(Map.Entry<String,String> e:r.getEnvironment().getValues().entrySet())put(b,"env."+e.getKey(),e.getValue());return b.toString();
 }
 public CrashReport decode(String text){
  if(text==null||!text.startsWith("ALPHA_CRASH_REPORT\\t1\\n"))throw new IllegalArgumentException("Unsupported report format.");
  Map<String,String> m=new LinkedHashMap<String,String>();String[] lines=text.split("\\n");
  for(int i=1;i<lines.length;i++){int p=lines[i].indexOf('=');if(p<1)continue;try{m.put(lines[i].substring(0,p),new String(Base64.getDecoder().decode(lines[i].substring(p+1)),UTF8));}catch(Exception ignored){}}
  Map<String,String> env=new LinkedHashMap<String,String>();for(Map.Entry<String,String> e:m.entrySet())if(e.getKey().startsWith("env."))env.put(e.getKey().substring(4),e.getValue());
  return new CrashReport(req(m,"id"),num(m.get("time")),m.get("title"),m.get("type"),m.get("message"),m.get("stack"),
   en(CrashSource.class,m.get("source"),CrashSource.UNKNOWN),en(CrashCategory.class,m.get("category"),CrashCategory.UNKNOWN),
   en(CrashSeverity.class,m.get("severity"),CrashSeverity.ERROR),m.get("module"),m.get("fingerprint"),m.get("client"),m.get("minecraft"),m.get("forge"),
   new CrashEnvironment(env),en(CrashReportStatus.class,m.get("status"),CrashReportStatus.NEW));
 }
 private static void put(StringBuilder b,String k,String v){b.append(k).append('=').append(Base64.getEncoder().encodeToString((v==null?"":v).getBytes(UTF8))).append('\\n');}
 private static String req(Map<String,String> m,String k){String v=m.get(k);if(v==null||v.isEmpty())throw new IllegalArgumentException("Missing "+k);return v;}
 private static long num(String s){try{return Long.parseLong(s);}catch(Exception e){return 0;}}
 private static <E extends Enum<E>> E en(Class<E> c,String s,E d){try{return Enum.valueOf(c,s);}catch(Exception e){return d;}}
}
