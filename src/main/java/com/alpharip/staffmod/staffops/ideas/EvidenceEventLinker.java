package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

/** Local association between evidence references and event IDs. */
public final class EvidenceEventLinker {
 private final Map<String,String> links=new LinkedHashMap<String,String>();
 public void link(String evidenceReference,String eventId){if(evidenceReference==null||evidenceReference.trim().isEmpty())throw new IllegalArgumentException("Evidence reference must not be blank");links.put(evidenceReference.trim(),eventId==null?"":eventId);}
 public String getEventId(String evidenceReference){return links.get(evidenceReference);}
 public Map<String,String> getLinks(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(links));}
}