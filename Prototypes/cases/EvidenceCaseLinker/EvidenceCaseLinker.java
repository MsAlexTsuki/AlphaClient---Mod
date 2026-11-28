package com.alpharip.staffmod.cases.evidencecaselinker;
import java.io.File;import java.util.*;
public final class EvidenceCaseLinker {
 private final EvidenceLinkService service=new EvidenceLinkService();private final EvidenceLinkStorage storage;
 public EvidenceCaseLinker(File file){storage=new EvidenceLinkStorage(file);}public EvidenceLinkService getService(){return service;}
 public boolean load(){return service.replaceAll(storage.load());}public boolean save(){return storage.save(service.getLinks());}
 /** Retains the original idea API while delegating to the structured link registry. */
 public void link(String evidenceReference,String caseId){service.link(new EvidenceLink(evidenceReference,caseId,"",System.currentTimeMillis()));}
 public String getCaseId(String evidenceReference){EvidenceLink e=service.get(evidenceReference);return e==null?null:e.getCaseId();}
 public Map<String,String> getLinks(){Map<String,String> out=new LinkedHashMap<String,String>();for(EvidenceLink e:service.getLinks())out.put(e.getEvidenceReference(),e.getCaseId());return Collections.unmodifiableMap(out);}
 public EvidenceCaseLinkerGui createGui(net.minecraft.client.gui.GuiScreen parent){return new EvidenceCaseLinkerGui(parent,this);}
}