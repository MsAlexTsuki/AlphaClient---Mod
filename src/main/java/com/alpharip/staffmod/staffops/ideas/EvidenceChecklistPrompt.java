package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Validates required evidence fields before a report is submitted. */
public final class EvidenceChecklistPrompt {
 public static final String NAME="Evidence Checklist Prompt",CATEGORY="TrialModerator",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Set<String> required=new LinkedHashSet<String>(Arrays.asList("player","rule","timestamp","evidence"));
 public synchronized void setRequired(Collection<String> fields){if(fields==null)throw new IllegalArgumentException("fields required");required.clear();for(String f:fields)required.add(req(f));}
 public synchronized List<String> missing(Map<String,String> supplied){List<String> out=new ArrayList<String>();for(String k:required)if(supplied==null||supplied.get(k)==null||supplied.get(k).trim().isEmpty())out.add(k);return Collections.unmodifiableList(out);}
 public synchronized boolean canSubmit(Map<String,String> supplied){return missing(supplied).isEmpty();}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("field required");return s.trim();}
}