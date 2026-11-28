package com.alpharip.staffmod.staffops.ideas;
/** Tracks whether the first-start intro has been shown; persistence and animation are external. */
public final class AlphaGenesisIntro {
 public static final String NAME="Alpha Genesis Intro",CATEGORY="FounderFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private boolean enabled=true,shown;
 public synchronized void setEnabled(boolean v){enabled=v;} public synchronized boolean isEnabled(){return enabled;}
 public synchronized boolean shouldPlay(){if(!enabled||shown)return false;shown=true;return true;}
 public synchronized void resetForTesting(){shown=false;}
}