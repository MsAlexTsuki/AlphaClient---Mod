package com.alpharip.staffmod.staffops.ideas;

/** Foundation for item slots and selection state in a radial quick-item menu. */
public class RadialQuickItemMenu {
 private boolean enabled; private int selectedSlot=-1; private final java.util.Map<Integer,String> items=new java.util.LinkedHashMap<Integer,String>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void assign(int slot,String itemId){if(slot>=0&&slot<9&&itemId!=null&&!itemId.trim().isEmpty())items.put(slot,itemId);}
 public void select(int slot){selectedSlot=items.containsKey(slot)?slot:-1;}
 public int getSelectedSlot(){return selectedSlot;} public String getSelectedItem(){return items.get(selectedSlot);}
 public java.util.Map<Integer,String> getItems(){return java.util.Collections.unmodifiableMap(items);}
}
