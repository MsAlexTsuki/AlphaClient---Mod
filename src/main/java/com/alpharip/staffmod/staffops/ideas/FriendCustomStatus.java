package com.alpharip.staffmod.staffops.ideas;
/** Local custom friend status. Publishing status to a social service is not connected. */
public class FriendCustomStatus {
 public enum Visibility { EVERYONE, FRIENDS, NOBODY }
 private String text="";private Visibility visibility=Visibility.FRIENDS;private long expiresAt;
 public String getText(){return text;}public void setText(String s){if(s!=null&&s.length()>80)throw new IllegalArgumentException("Status must be 80 characters or fewer");text=s==null?"":s.trim();}
 public Visibility getVisibility(){return visibility;}public void setVisibility(Visibility v){if(v==null)throw new IllegalArgumentException("Visibility required");visibility=v;}
 public long getExpiresAt(){return expiresAt;}public void setExpiresAt(long t){expiresAt=Math.max(0,t);}public boolean isExpired(long now){return expiresAt>0&&now>=expiresAt;}
 public boolean isVisibleToFriends(){return visibility==Visibility.FRIENDS||visibility==Visibility.EVERYONE;}
}