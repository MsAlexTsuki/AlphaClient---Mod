package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.UUID;
/** Metadata reference only; does not copy, upload, or verify evidence files. */
public final class AppealEvidence {
 private final String id,label,reference,addedBy;private final long addedAt;
 public AppealEvidence(String label,String reference,String by,long at){
  if(label==null||label.trim().isEmpty()||reference==null||reference.trim().isEmpty()||at<=0)throw new IllegalArgumentException("Valid label, reference and timestamp required");
  id=UUID.randomUUID().toString();this.label=label.trim();this.reference=reference.trim();addedBy=by==null?"":by.trim();addedAt=at;
 }
 public String getId(){return id;}public String getLabel(){return label;}public String getReference(){return reference;}public String getAddedBy(){return addedBy;}public long getAddedAt(){return addedAt;}
}