package com.alpharip.staffmod.staffops.ideas;

/** Foundation for warning about potentially fake verification messages. */
public class AntiPhishingWarningForFakeVerificationMessages {
 private boolean enabled=true; private int warningsIssued;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public boolean shouldWarn(boolean asksForPasswordOrCode,boolean untrustedSender){return enabled&&(asksForPasswordOrCode||untrustedSender);}
 public void recordWarning(){if(enabled)warningsIssued++;}
 public int getWarningsIssued(){return warningsIssued;}
}
