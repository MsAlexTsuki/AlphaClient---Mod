package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.*;
public final class AppealReviewService {
 private final AppealReviewBoard board;
 public AppealReviewService(AppealReviewBoard b){if(b==null)throw new IllegalArgumentException("Board required");board=b;}
 public AppealCase submit(String player,String punishment,String reason,long now){AppealCase a=AppealCase.create(player,punishment,reason,now);board.add(a);return a;}
 public void assign(String id,String reviewer,long now){AppealCase a=require(id);a.assign(reviewer,now);if(a.getStatus()==AppealStatus.SUBMITTED)a.status(AppealStatus.UNDER_REVIEW,now);board.addRelated(id,new AppealReviewerAssignment(id,reviewer,now));}
 public void setStatus(String id,AppealStatus s,long now){require(id).status(s,now);}
 public void addEvidence(String id,String label,String ref,String by,long now){board.addRelated(id,new AppealEvidence(label,ref,by,now));}
 public void addNote(String id,String by,String text,long now){board.addRelated(id,new AppealReviewNote(by,text,now));}
 public void decide(String id,AppealDecisionType type,String by,String rationale,long now){
  AppealCase a=require(id);board.addRelated(id,new AppealDecisionRecord(id,type,by,rationale,now));
  if(type==AppealDecisionType.APPROVE)a.status(AppealStatus.APPROVED,now);
  else if(type==AppealDecisionType.DENY)a.status(AppealStatus.DENIED,now);
  else if(type==AppealDecisionType.REQUEST_INFORMATION)a.status(AppealStatus.NEEDS_INFORMATION,now);
  else if(type==AppealDecisionType.ESCALATE)escalate(id,rationale,by,"",now);
  else requestReopen(id,rationale,by,now);
 }
 public void escalate(String id,String reason,String by,String target,long now){require(id);board.addRelated(id,new AppealEscalationRecord(id,reason,by,target,now));require(id).status(AppealStatus.ESCALATED,now);}
 public AppealReopenRequest requestReopen(String id,String reason,String by,long now){require(id);AppealReopenRequest r=new AppealReopenRequest(id,reason,by,now);board.addRelated(id,r);return r;}
 public void resolveReopen(String id,AppealReopenRequest r,boolean accepted,long now){require(id);if(r==null||!id.equals(r.getAppealId()))throw new IllegalArgumentException("Matching reopen request required");r.resolve(accepted);if(accepted)require(id).status(AppealStatus.UNDER_REVIEW,now);}
 public boolean remove(String id){return board.remove(id);}
 public List<AppealCase> filter(AppealReviewFilter f,AppealReviewSort sort){
  List<AppealCase> out=new ArrayList<AppealCase>();for(AppealCase a:board.getAll()){
   if(f!=null&&f.getStatus()!=null&&a.getStatus()!=f.getStatus())continue;
   if(f!=null&&!a.getPlayerName().toLowerCase(Locale.ROOT).contains(f.getPlayer()))continue;
   if(f!=null&&!a.getAssignedReviewer().toLowerCase(Locale.ROOT).contains(f.getReviewer()))continue;out.add(a);
  }
  final AppealReviewSort order=sort==null?AppealReviewSort.NEWEST:sort;
  Collections.sort(out,new Comparator<AppealCase>(){public int compare(AppealCase a,AppealCase b){
   if(order==AppealReviewSort.PLAYER_NAME)return a.getPlayerName().compareToIgnoreCase(b.getPlayerName());
   long x=order==AppealReviewSort.OLDEST?a.getSubmittedAt():order==AppealReviewSort.UPDATED_RECENTLY?a.getUpdatedAt():-a.getSubmittedAt();
   long y=order==AppealReviewSort.OLDEST?b.getSubmittedAt():order==AppealReviewSort.UPDATED_RECENTLY?b.getUpdatedAt():-b.getSubmittedAt();return x<y?-1:x==y?0:1;}});
  return Collections.unmodifiableList(out);
 }
 public AppealReviewSummary summarize(){int s=0,r=0,e=0,d=0;for(AppealCase a:board.getAll())switch(a.getStatus()){case SUBMITTED:s++;break;case UNDER_REVIEW:case NEEDS_INFORMATION:r++;break;case ESCALATED:e++;break;default:d++;}return new AppealReviewSummary(board.getAll().size(),s,r,e,d);}
 private AppealCase require(String id){AppealCase a=board.get(id);if(a==null)throw new IllegalArgumentException("Appeal not found");return a;}
}