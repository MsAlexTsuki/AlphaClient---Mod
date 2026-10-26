package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

/** Local archive index of evidence references; no files are moved or deleted. */
public final class EvidenceArchive {
 private final Map<String,Long> archivedAt=new LinkedHashMap<String,Long>();
 public void archive(String reference,long timestampMillis){if(reference==null||reference.trim().isEmpty())throw new IllegalArgumentException("Evidence reference must not be blank");archivedAt.put(reference.trim(),timestampMillis);}
 public boolean isArchived(String reference){return archivedAt.containsKey(reference);}
 public Long getArchivedAt(String reference){return archivedAt.get(reference);}
 public Map<String,Long> getEntries(){return Collections.unmodifiableMap(new LinkedHashMap<String,Long>(archivedAt));}
 // TODO(Supabase): define retention, authorization, and audit policy before shared archiving.
}