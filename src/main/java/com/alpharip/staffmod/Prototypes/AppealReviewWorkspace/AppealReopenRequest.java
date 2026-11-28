package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.UUID;
public final class AppealReopenRequest {
 public enum State { PENDING, ACCEPTED, REJECTED }
 private final String id,appealId,reason,requestedBy;private final long requestedAt;private State state=State.PENDING;
 public AppealReopenRequest(String appeal,String reason,String by,long at){if(appeal==null||appeal.trim().isEmpty()||reason==null||reason.trim().isEmpty()||at<=0)throw new IllegalArgumentException("Appeal, reason and timestamp required");id=UUID.randomUUID().toString();appealId=appeal.trim();this.reason=reason.trim();requestedBy=by==null?"":by.trim();requestedAt=at;}
 public String getId(){return id;}public String getAppealId(){return appealId;}public String getReason(){return reason;}public String getRequestedBy(){return requestedBy;}public long getRequestedAt(){return requestedAt;}public State getState(){return state;}
 void resolve(boolean accepted){state=accepted?State.ACCEPTED:State.REJECTED;}
}