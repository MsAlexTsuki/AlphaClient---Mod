package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking arrows available to the player. */
public class ArrowCounter {
 private boolean enabled=true; private int arrowCount;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getArrowCount(){return arrowCount;} public void setArrowCount(int v){arrowCount=Math.max(0,v);}
 public void refreshFromInventory(int[] stackCounts){int total=0;if(stackCounts!=null)for(int count:stackCounts)if(count>0)total+=count;arrowCount=total;}
}
