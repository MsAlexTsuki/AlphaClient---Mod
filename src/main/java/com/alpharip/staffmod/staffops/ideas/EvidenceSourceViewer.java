package com.alpharip.staffmod.staffops.ideas;

/** View-model for source metadata associated with an evidence reference. */
public final class EvidenceSourceViewer {
 private String sourceType="",sourceReference="",description="";
 public void setSource(String sourceType,String sourceReference,String description){this.sourceType=sourceType==null?"":sourceType;this.sourceReference=sourceReference==null?"":sourceReference;this.description=description==null?"":description;}
 public String getSourceType(){return sourceType;} public String getSourceReference(){return sourceReference;} public String getDescription(){return description;}
}