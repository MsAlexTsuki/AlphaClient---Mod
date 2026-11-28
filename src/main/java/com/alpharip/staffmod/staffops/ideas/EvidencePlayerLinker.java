package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

/** Local association between evidence references and caller-supplied player IDs. */
public final class EvidencePlayerLinker {
 private final Map<String,String> links=new LinkedHashMap<String,String>();
 public void link(String evidenceReference,String playerId){if(evidenceReference==null||evidenceReference.trim().isEmpty())throw new IllegalArgumentException("Evidence reference must not be blank");links.put(evidenceReference.trim(),playerId==null?"":playerId);}
 public String getPlayerId(String evidenceReference){return links.get(evidenceReference);}
 public Map<String,String> getLinks(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(links));}
}