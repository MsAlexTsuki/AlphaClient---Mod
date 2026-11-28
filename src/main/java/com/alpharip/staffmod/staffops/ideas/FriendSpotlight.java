package com.alpharip.staffmod.staffops.ideas;

/** Holds the locally selected friend spotlight; social lookup and rendering are external. */
public final class FriendSpotlight {
 public static final String FEATURE_NAME="Friend Spotlight",CATEGORY="Donator Alpha",STATUS="NEW";
 private String friendId; private boolean enabled;
 public void spotlight(String id){if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("friend id required");friendId=id.trim();enabled=true;}
 public void clear(){friendId=null;enabled=false;}
 public boolean isEnabled(){return enabled;} public String getFriendId(){return friendId;}
}