package com.alpharip.staffmod.AppealReviewWorkspace;
import java.io.File;
/** Coordinates local state and an externally supplied StaffMods access adapter. */
public final class AppealReviewController {
 private final AppealReviewBoard board;private final AppealReviewService service;private final AppealReviewAccess access;private final File file;
 public AppealReviewController(AppealReviewBoard b,AppealReviewAccess a,File f){if(b==null||a==null)throw new IllegalArgumentException("Board and access adapter required");board=b;service=new AppealReviewService(b);access=a;file=f;}
 public void load(){if(access.canViewAppeals()&&file!=null)AppealReviewStorage.load(file,board);}
 public boolean save(){return file!=null&&AppealReviewStorage.save(file,board);}
 public AppealReviewBoard getBoard(){return board;}public AppealReviewService getService(){return service;}public AppealReviewAccess getAccess(){return access;}
}