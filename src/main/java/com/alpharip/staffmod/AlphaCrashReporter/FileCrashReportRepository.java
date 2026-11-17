package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.*;import java.nio.charset.Charset;import java.nio.file.*;import java.util.*;
/** Local file repository: one .acr file per crash report. */
public final class FileCrashReportRepository implements CrashReportRepository {
 private static final Charset UTF8=Charset.forName("UTF-8");private final File dir;private final CrashReportCodec codec;
 public FileCrashReportRepository(File d,CrashReportCodec c){if(d==null)throw new IllegalArgumentException("Directory required.");dir=d;codec=c==null?new CrashReportCodec():c;}
 public synchronized void save(CrashReport r)throws IOException{
  if(r==null)throw new IllegalArgumentException("Report required.");if(!dir.exists()&&!dir.mkdirs())throw new IOException("Cannot create report directory.");
  File target=file(r.getId()),tmp=new File(dir,r.getId()+".tmp");Files.write(tmp.toPath(),codec.encode(r).getBytes(UTF8));
  try{Files.move(tmp.toPath(),target.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
  catch(IOException e){Files.move(tmp.toPath(),target.toPath(),StandardCopyOption.REPLACE_EXISTING);}
 }
 public synchronized List<CrashReport> loadAll()throws IOException{
  List<CrashReport> out=new ArrayList<CrashReport>();if(!dir.exists())return out;
  File[] fs=dir.listFiles((d,n)->n.endsWith(".acr"));if(fs==null)return out;
  for(File f:fs)try{out.add(codec.decode(new String(Files.readAllBytes(f.toPath()),UTF8)));}catch(Exception ignored){}
  Collections.sort(out,(a,b)->Long.compare(b.getTimestamp(),a.getTimestamp()));return out;
 }
 public synchronized void delete(String id)throws IOException{
  if(id==null||id.contains("/")||id.contains("\\\\"))throw new IllegalArgumentException("Invalid report id.");
  File f=file(id);if(f.exists()&&!f.delete())throw new IOException("Could not delete report.");
 }
 private File file(String id){return new File(dir,id+".acr");}
}
