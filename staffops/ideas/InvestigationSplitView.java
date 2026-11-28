package com.alpharip.staffmod.staffops.ideas;

/** Layout state for a two-pane investigation workspace. */
public final class InvestigationSplitView {
 private int splitPercent=50;
 private boolean leftVisible=true,rightVisible=true;
 public int getSplitPercent(){return splitPercent;}
 public void setSplitPercent(int percent){if(percent<10||percent>90)throw new IllegalArgumentException("Split must be between 10 and 90");splitPercent=percent;}
 public boolean isLeftVisible(){return leftVisible;} public void setLeftVisible(boolean visible){leftVisible=visible;}
 public boolean isRightVisible(){return rightVisible;} public void setRightVisible(boolean visible){rightVisible=visible;}
}