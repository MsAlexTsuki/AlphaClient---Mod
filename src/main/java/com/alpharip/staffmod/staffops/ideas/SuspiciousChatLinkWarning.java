package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking suspicious chat links flagged for user warning. */
public class SuspiciousChatLinkWarning {
 private boolean enabled=true; private final java.util.Set<String> warnedLinks=new java.util.LinkedHashSet<String>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public boolean shouldWarn(String link,boolean flaggedSuspicious){return enabled&&flaggedSuspicious&&link!=null&&!link.trim().isEmpty();}
 public void recordWarning(String link){if(link!=null&&!link.trim().isEmpty())warnedLinks.add(link);}
 public java.util.Set<String> getWarnedLinks(){return java.util.Collections.unmodifiableSet(warnedLinks);}
}
