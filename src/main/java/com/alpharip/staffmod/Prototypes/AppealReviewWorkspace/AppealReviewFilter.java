package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.Locale;
public final class AppealReviewFilter {
 private final AppealStatus status;private final String player,reviewer;
 public AppealReviewFilter(AppealStatus status,String player,String reviewer){this.status=status;this.player=clean(player);this.reviewer=clean(reviewer);}
 public AppealStatus getStatus(){return status;}public String getPlayer(){return player;}public String getReviewer(){return reviewer;}
 private static String clean(String s){return s==null?"":s.trim().toLowerCase(Locale.ROOT);}
}