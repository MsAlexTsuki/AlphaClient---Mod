package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

/** Local links between evidence references and case identifiers; no shared case storage. */
public final class EvidenceCaseLinker {
 private final Map<String,String> links=new LinkedHashMap<String,String>();
 public void link(String evidenceReference,String caseId){if(evidenceReference==null||evidenceReference.trim().isEmpty())throw new IllegalArgumentException("Evidence reference must not be blank");links.put(evidenceReference.trim(),caseId==null?"":caseId);}
 public String getCaseId(String evidenceReference){return links.get(evidenceReference);}
 public Map<String,String> getLinks(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(links));}
 // TODO(Supabase): define authorization and shared case storage before remote linking.
}