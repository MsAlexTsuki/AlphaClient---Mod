package com.alpharip.staffmod.BuildTimelapse.export;
import java.io.File;import java.util.UUID;
public final class ExportJob {
 private final String id=UUID.randomUUID().toString();private final File frames,output;private final ExportSettings settings;
 public ExportJob(File f,File o,ExportSettings s){if(f==null||o==null||s==null)throw new IllegalArgumentException();frames=f;output=o;settings=s.copy();}
 public String getId(){return id;}public File getFrames(){return frames;}public File getOutput(){return output;}public ExportSettings getSettings(){return settings.copy();}
}