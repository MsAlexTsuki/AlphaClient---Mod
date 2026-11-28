package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local in-memory queue of evidence references awaiting review. */
public final class EvidenceReviewQueue {
 private final List<String> references=new ArrayList<String>();
 public void enqueue(String reference){if(reference!=null&&!reference.trim().isEmpty()&&!references.contains(reference.trim()))references.add(reference.trim());}
 public boolean remove(String reference){return references.remove(reference);}
 public List<String> getReferences(){return Collections.unmodifiableList(new ArrayList<String>(references));}
 public boolean isEmpty(){return references.isEmpty();}
 // TODO(Supabase): define shared queue access and authorization before remote use.
}