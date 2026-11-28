package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.UUID;
public final class AppealEscalationRecord {
 private final String id,appealId,reason,by,target;private final long createdAt;
 public AppealEscalationRecord(String appeal,String reason,String by,String target,long at){if(appeal==null||appeal.trim().isEmpty()||reason==null||reason.trim().isEmpty()||at<=0)throw new IllegalArgumentException("Appeal, reason and timestamp required");id=UUID.randomUUID().toString();appealId=appeal.trim();this.reason=reason.trim();this.by=by==null?"":by.trim();this.target=target==null?"":target.trim();createdAt=at;}
 public String getId(){return id;}public String getAppealId(){return appealId;}public String getReason(){return reason;}public String getBy(){return by;}public String getTarget(){return target;}public long getCreatedAt(){return createdAt;}
}