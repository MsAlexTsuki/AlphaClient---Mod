package com.alpharip.staffmod.cases.detectioncaseautolinker;
import java.io.File;import java.util.*;
public final class DetectionCaseAutoLinker {
 private final DetectionLinkService service=new DetectionLinkService();private final DetectionLinkStorage storage;private boolean enabled=true;private final List<String> records=new ArrayList<String>();
 public DetectionCaseAutoLinker(File file){storage=new DetectionLinkStorage(file);}
 public DetectionLinkService getService(){return service;}
 public boolean load(){service.setRequiredKeys(storage.loadKeys());return service.replaceAll(storage.load());}
 public boolean save(){return storage.save(service.getCases(),service.getRequiredKeys());}
 public void addRecord(String record){if(record!=null&&!record.trim().isEmpty())records.add(record);}
 public List<String> getRecords(){return Collections.unmodifiableList(new ArrayList<String>(records));}
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean value){enabled=value;}public void clear(){records.clear();}
 public DetectionCaseAutoLinkerGui createGui(net.minecraft.client.gui.GuiScreen parent){return new DetectionCaseAutoLinkerGui(parent,this);}
}