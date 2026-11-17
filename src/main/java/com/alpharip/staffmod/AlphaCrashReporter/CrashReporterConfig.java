package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.File;
/** In-memory settings; connect to the client config system during integration. */
public final class CrashReporterConfig {
 private boolean enabled=true,stackTraces=true,redact=true,groupDuplicates=true;
 private int maxReports=100; private File exportDirectory=new File("AlphaCrashReporter/exports");
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public boolean isCaptureStackTraces(){return stackTraces;} public void setCaptureStackTraces(boolean v){stackTraces=v;}
 public boolean isRedactSensitiveData(){return redact;} public void setRedactSensitiveData(boolean v){redact=v;}
 public boolean isGroupDuplicates(){return groupDuplicates;} public void setGroupDuplicates(boolean v){groupDuplicates=v;}
 public int getMaxReports(){return maxReports;} public void setMaxReports(int v){maxReports=Math.max(1,Math.min(5000,v));}
 public File getExportDirectory(){return exportDirectory;} public void setExportDirectory(File f){if(f!=null)exportDirectory=f;}
}
