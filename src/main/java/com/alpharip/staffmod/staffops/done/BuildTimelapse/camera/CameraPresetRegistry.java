package com.alpharip.staffmod.BuildTimelapse.camera;
import java.util.*;
public final class CameraPresetRegistry {
 private final Map<String,CameraPose> values=new LinkedHashMap<String,CameraPose>();
 public synchronized void save(String name,CameraPose pose){if(name==null||name.trim().isEmpty()||pose==null)throw new IllegalArgumentException();values.put(name.trim(),pose);}
 public synchronized CameraPose get(String name){return name==null?null:values.get(name.trim());}
 public synchronized Map<String,CameraPose> all(){return Collections.unmodifiableMap(new LinkedHashMap<String,CameraPose>(values));}
 public synchronized boolean remove(String name){return name!=null&&values.remove(name.trim())!=null;}
}