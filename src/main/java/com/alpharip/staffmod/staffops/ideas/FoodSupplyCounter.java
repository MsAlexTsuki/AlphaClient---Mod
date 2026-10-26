package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking food supplies across inventory slots. */
public class FoodSupplyCounter {
 private boolean enabled=true; private int foodItems;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getFoodItems(){return foodItems;} public void setFoodItems(int v){foodItems=Math.max(0,v);}
 public void refresh(int[] foodStackCounts){int total=0;if(foodStackCounts!=null)for(int count:foodStackCounts)if(count>0)total+=count;foodItems=total;}
}
