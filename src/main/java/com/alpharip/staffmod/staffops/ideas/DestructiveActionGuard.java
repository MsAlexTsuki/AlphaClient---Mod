package com.alpharip.staffmod.staffops.ideas;
/** Local helper for requiring an explicit typed confirmation before destructive actions. */
public final class DestructiveActionGuard {
 public static final String NAME="Destructive Action Guard", CATEGORY="Security", STATUS="NEW";
 public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=false;
 public boolean isConfirmed(String expected,String supplied){return expected!=null&&supplied!=null&&expected.equals(supplied);}
}