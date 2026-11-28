package com.alpharip.staffmod.staffops.ideas;
/** Local friend-privacy preferences. Social service integration is not connected. */
public class FriendPrivacySettings {
 private boolean allowRequests=true,showOnline=true,shareServer=true,allowInvites=true,showStatus=true;
 public boolean allowsRequests(){return allowRequests;}public void setAllowRequests(boolean v){allowRequests=v;}
 public boolean showsOnline(){return showOnline;}public void setShowOnline(boolean v){showOnline=v;}
 public boolean sharesServer(){return shareServer;}public void setShareServer(boolean v){shareServer=v;}
 public boolean allowsInvites(){return allowInvites;}public void setAllowInvites(boolean v){allowInvites=v;}
 public boolean showsStatus(){return showStatus;}public void setShowStatus(boolean v){showStatus=v;}
}