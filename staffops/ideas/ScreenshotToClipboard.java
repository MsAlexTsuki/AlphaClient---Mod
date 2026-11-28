package com.alpharip.staffmod.staffops.ideas;
/** Clipboard transfer settings; actual OS clipboard integration is not connected. */
public class ScreenshotToClipboard {
 private boolean enabled=true,includeCursor;private String format="PNG";public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public boolean isIncludeCursor(){return includeCursor;}public void setIncludeCursor(boolean v){includeCursor=v;}public String getPreferredFormat(){return format;}
 public void setPreferredFormat(String f){if(!"PNG".equalsIgnoreCase(f)&&!"JPEG".equalsIgnoreCase(f))throw new IllegalArgumentException("Format must be PNG or JPEG");format=f.toUpperCase(java.util.Locale.ROOT);}
 public boolean canCopy(byte[] bytes){return enabled&&bytes!=null&&bytes.length>0;}
}