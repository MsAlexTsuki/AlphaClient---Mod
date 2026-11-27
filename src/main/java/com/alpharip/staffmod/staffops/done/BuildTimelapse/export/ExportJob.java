package com.alpharip.staffmod.BuildTimelapse.export;
import java.io.File;import java.util.UUID;
public final class ExportJob {
 private final String id;private final File frames,output;private final ExportSettings settings;
 public ExportJob(File f,File o,ExportSettings s){this(f,o,s,UUID.randomUUID().toString());}
 public ExportJob(File f,File o,ExportSettings s,String id){if(f==null||o==null||s==null||id==null||!id.matches("[A-Za-z0-9_-]{1,80}"))throw new IllegalArgumentException();frames=f;output=o;settings=s.copy();this.id=id;}
 public String getId(){return id;}public File getFrames(){return frames;}public File getOutput(){return output;}public ExportSettings getSettings(){return settings.copy();}
}
