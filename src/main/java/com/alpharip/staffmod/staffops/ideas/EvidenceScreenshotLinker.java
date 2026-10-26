package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

/** Local association between evidence IDs and screenshot references; does not capture files. */
public final class EvidenceScreenshotLinker {
 private final Map<String,String> links=new LinkedHashMap<String,String>();
 public void link(String evidenceId,String screenshotReference){if(evidenceId==null||evidenceId.trim().isEmpty())throw new IllegalArgumentException("Evidence ID must not be blank");links.put(evidenceId.trim(),screenshotReference==null?"":screenshotReference);}
 public String getScreenshotReference(String evidenceId){return links.get(evidenceId);}
 public Map<String,String> getLinks(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(links));}
}