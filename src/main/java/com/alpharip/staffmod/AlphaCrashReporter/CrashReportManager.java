package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.IOException;import java.util.*;
public final class CrashReportManager {
 private final CrashReportRepository repository;private final CrashReporterConfig config;
 public CrashReportManager(CrashReportRepository r,CrashReporterConfig c){repository=r;config=c;}
 public synchronized void add(CrashReport r)throws IOException{repository.save(r);List<CrashReport> all=repository.loadAll();for(int i=config.getMaxReports();i<all.size();i++)repository.delete(all.get(i).getId());}
 public List<CrashReport> getAll()throws IOException{return repository.loadAll();}
 public CrashReport get(String id)throws IOException{for(CrashReport r:getAll())if(r.getId().equals(id))return r;return null;}
 public void delete(String id)throws IOException{repository.delete(id);}
}
