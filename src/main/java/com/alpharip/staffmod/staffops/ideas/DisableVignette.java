package com.alpharip.staffmod.staffops.ideas;

/** Foundation for the option to disable the screen-edge vignette effect. */
public class DisableVignette {
 private boolean disabled=true;
 public boolean isDisabled(){return disabled;} public void setDisabled(boolean v){disabled=v;}
 public boolean shouldRenderVignette(){return !disabled;}
}
