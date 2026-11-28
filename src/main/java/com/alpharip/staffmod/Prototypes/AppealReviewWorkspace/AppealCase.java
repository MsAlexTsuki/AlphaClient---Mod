package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.UUID;
/** One appeal and its current review state. */
public final class AppealCase {
 private final String id,playerName,originalPunishment,appealReason; private final long submittedAt;
 private long updatedAt; private AppealStatus status=AppealStatus.SUBMITTED; private String assignedReviewer="";
 public AppealCase(String id,String player,String punishment,String reason,long now){
  this.id=req(id);playerName=req(player);originalPunishment=clean(punishment);appealReason=req(reason);
  if(now<=0)throw new IllegalArgumentException("Timestamp must be positive");submittedAt=now;updatedAt=now;
 }
 public static AppealCase create(String p,String punishment,String reason,long now){return new AppealCase(UUID.randomUUID().toString(),p,punishment,reason,now);}
 public String getId(){return id;} public String getPlayerName(){return playerName;} public String getOriginalPunishment(){return originalPunishment;}
 public String getAppealReason(){return appealReason;} public long getSubmittedAt(){return submittedAt;} public long getUpdatedAt(){return updatedAt;}
 public AppealStatus getStatus(){return status;} public String getAssignedReviewer(){return assignedReviewer;}
 void status(AppealStatus s,long now){if(s==null)throw new IllegalArgumentException("Status required");status=s;touch(now);}
 void assign(String s,long now){assignedReviewer=clean(s);touch(now);}
 private void touch(long n){if(n<=0)throw new IllegalArgumentException("Timestamp must be positive");updatedAt=n;}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("Required value missing");return s.trim();}
 private static String clean(String s){return s==null?"":s.trim();}
}