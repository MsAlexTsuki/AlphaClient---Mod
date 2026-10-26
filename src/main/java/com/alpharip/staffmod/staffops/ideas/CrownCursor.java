package com.alpharip.staffmod.staffops.ideas;
/** User preference for the crown cursor texture; custom cursor rendering is not wired here. */
public final class CrownCursor {
 public static final String NAME="Crown Cursor",CATEGORY="OwnerFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private boolean enabled; private String cursorId="crown_default";
 public synchronized void setEnabled(boolean v){enabled=v;} public synchronized boolean isEnabled(){return enabled;}
 public synchronized void setCursorId(String id){if(id==null||!id.matches("[a-zA-Z0-9_:-]{1,64}"))throw new IllegalArgumentException("invalid cursor id");cursorId=id;}
 public synchronized String getCursorId(){return cursorId;}
}