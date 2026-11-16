package com.alpharip.staffmod.cases.caseredactiontool;
import java.io.File;import java.util.*;
/** Standalone local redaction prototype. No remote case access. */
public final class CaseRedactionTool {
 private final RedactionService service=new RedactionService();private final RedactionStorage storage;private boolean enabled=true;private final List<String> records=new ArrayList<String>();
 public CaseRedactionTool(File file){storage=new RedactionStorage(file);}public RedactionService getService(){return service;}
 public boolean load(){return service.replaceAll(storage.load());}public boolean save(){return storage.save(service.getDocuments());}
 public void addRecord(String record){if(record!=null&&!record.trim().isEmpty())records.add(record);}public List<String> getRecords(){return Collections.unmodifiableList(new ArrayList<String>(records));}
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean value){enabled=value;}public void clear(){records.clear();}
 public CaseRedactionToolGui createGui(net.minecraft.client.gui.GuiScreen parent){return new CaseRedactionToolGui(parent,this);}
}