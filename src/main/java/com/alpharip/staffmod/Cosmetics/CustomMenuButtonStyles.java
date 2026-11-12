package com.alpharip.staffmod.staffops.ideas;

/** Foundation for selecting menu button styles. */
public class CustomMenuButtonStyles {
 public enum Style { VANILLA, FLAT, ROUNDED, OUTLINED }
 private Style style=Style.VANILLA; private int accentColor=0xFFFFC83D;
 public Style getStyle(){return style;} public void setStyle(Style v){style=v==null?Style.VANILLA:v;}
 public int getAccentColor(){return accentColor;} public void setAccentColor(int v){accentColor=v;}
}
