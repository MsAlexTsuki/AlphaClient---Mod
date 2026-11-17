package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.*;import java.nio.charset.Charset;import java.nio.file.Files;import java.text.SimpleDateFormat;import java.util.*;
public final class CrashExportService {
 private final File directory;public CrashExportService(File d){directory=d==null?new File("AlphaCrashReporter/exports"):d;}
 public File export(CrashReport r)throws IOException{
  if(r==null)throw new IllegalArgumentException("Report required.");if(!directory.exists()&&!directory.mkdirs())throw new IOException("Cannot create export directory.");
  String stamp=new SimpleDateFormat("yyyyMMdd-HHmmss",Locale.ROOT).format(new Date(r.getTimestamp()));
  File f=new File(directory,"crash-"+stamp+"-"+r.getId().replaceAll("[^A-Za-z0-9-]","")+".txt");
  Files.write(f.toPath(),format(r).getBytes(Charset.forName("UTF-8")));return f;
 }
 public String format(CrashReport r){
  StringBuilder b=new StringBuilder("ALPHA CRASH REPORT\\n===================\\n");
  line(b,"ID",r.getId());line(b,"Date",new Date(r.getTimestamp()).toString());line(b,"Title",r.getTitle());line(b,"Exception",r.getExceptionType());
  line(b,"Category",r.getCategory().name());line(b,"Severity",r.getSeverity().name());line(b,"Source",r.getSource().name());
  line(b,"Module",r.getModuleName());line(b,"Alpha Client",r.getClientVersion());line(b,"Minecraft",r.getMinecraftVersion());line(b,"Forge",r.getForgeVersion());
  line(b,"Fingerprint",r.getFingerprint());line(b,"Status",r.getStatus().name());
  b.append("\\nMESSAGE\\n-------\\n").append(r.getMessage()).append("\\n\\nENVIRONMENT\\n-----------\\n");
  for(Map.Entry<String,String> e:r.getEnvironment().getValues().entrySet())line(b,e.getKey(),e.getValue());
  return b.append("\\nSTACK TRACE\\n-----------\\n").append(r.getStackTrace()).toString();
 }
 private static void line(StringBuilder b,String k,String v){b.append(k).append(": ").append(v==null?"":v).append('\\n');}
}
