package com.alpharip.staffmod.staffops.ideas;

/** Foundation for calculating the total count of one item across inventory slots. */
public class TotalHeldItemCountAcrossInventory {
 private String itemId="";
 public String getItemId(){return itemId;} public void setItemId(String v){itemId=v==null?"":v;}
 public int totalCount(int[] slotCounts){if(slotCounts==null)return 0;int total=0;for(int count:slotCounts)if(count>0)total+=count;return total;}
}
