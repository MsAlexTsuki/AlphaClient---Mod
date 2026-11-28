package com.alpharip.staffmod.staffops.ideas;

/** Foundation for selecting custom hunger icon sets. */
public class CustomHungerIcons {
 public enum IconSet { VANILLA, OUTLINE, COMPACT, MINIMAL }
 private IconSet iconSet=IconSet.VANILLA;
 public IconSet getIconSet(){return iconSet;} public void setIconSet(IconSet v){iconSet=v==null?IconSet.VANILLA:v;}
}
