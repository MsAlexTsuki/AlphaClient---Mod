package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local in-memory annotations attached to evidence references. */
public final class EvidenceAnnotationManager {
 private final List<Annotation> annotations=new ArrayList<Annotation>(); private long nextId=1L;
 public Annotation add(String evidenceReference,String text,long createdAtMillis) { if(text==null||text.trim().isEmpty()) throw new IllegalArgumentException("Annotation text must not be blank"); Annotation a=new Annotation(nextId++,evidenceReference,text.trim(),createdAtMillis); annotations.add(a); return a; }
 public List<Annotation> getAnnotations(){return Collections.unmodifiableList(new ArrayList<Annotation>(annotations));}
 public static final class Annotation {
  private final long id; private final String evidenceReference; private final String text; private final long createdAtMillis;
  private Annotation(long id,String evidenceReference,String text,long createdAtMillis){this.id=id;this.evidenceReference=evidenceReference==null?"":evidenceReference;this.text=text;this.createdAtMillis=createdAtMillis;}
  public long getId(){return id;} public String getEvidenceReference(){return evidenceReference;} public String getText(){return text;} public long getCreatedAtMillis(){return createdAtMillis;}
 }
}