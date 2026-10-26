package com.alpharip.staffmod.staffops.ideas;
/** Secret-menu unlock state; caller must enforce access and render the destination. */
public final class HiddenDoorMenu {
 public static final String NAME="Hidden Door Menu",CATEGORY="FounderFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final String secretCode;private boolean unlocked;
 public HiddenDoorMenu(String code){if(code==null||code.trim().length()<3)throw new IllegalArgumentException("secret code must be at least 3 characters");secretCode=code;}
 public synchronized boolean attempt(String input){if(input!=null&&secretCode.equals(input.trim()))unlocked=true;return unlocked;}
 public synchronized boolean isUnlocked(){return unlocked;}
 public synchronized void reset(){unlocked=false;}
}