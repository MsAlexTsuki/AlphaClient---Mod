package com.alpharip.staffmod.staffops.ideas;

/** Foundation for mapping item rarity levels to tooltip colors. */
public class RarityBasedTooltipColors {
 public enum Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }
 private boolean enabled=true;
 private final java.util.Map<Rarity,Integer> colors=new java.util.EnumMap<Rarity,Integer>(Rarity.class);
 public RarityBasedTooltipColors(){colors.put(Rarity.COMMON,0xFFFFFFFF);colors.put(Rarity.UNCOMMON,0xFF55FF55);colors.put(Rarity.RARE,0xFF5555FF);colors.put(Rarity.EPIC,0xFFAA00AA);colors.put(Rarity.LEGENDARY,0xFFFFAA00);}
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void setColor(Rarity rarity,int argb){if(rarity!=null)colors.put(rarity,argb);}
 public int getColor(Rarity rarity,int fallback){Integer c=colors.get(rarity);return enabled&&c!=null?c:fallback;}
}
