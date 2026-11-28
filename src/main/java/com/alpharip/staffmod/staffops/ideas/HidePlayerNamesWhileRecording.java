package com.alpharip.staffmod.staffops.ideas;

/** Foundation for hiding player names while recording or streaming. */
public class HidePlayerNamesWhileRecording {
 private boolean enabled; private boolean recording;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public boolean isRecording(){return recording;} public void setRecording(boolean v){recording=v;}
 public boolean shouldHideNames(){return enabled&&recording;}
}
