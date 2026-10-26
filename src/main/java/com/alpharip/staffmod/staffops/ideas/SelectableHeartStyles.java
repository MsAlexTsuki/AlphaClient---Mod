package com.alpharip.staffmod.staffops.ideas;

/** Foundation for choosing a health-heart display style. */
public class SelectableHeartStyles {
 public enum Style { CLASSIC, FLAT, OUTLINE, MINIMAL }
 private Style style=Style.CLASSIC;
 public Style getStyle(){return style;} public void setStyle(Style v){style=v==null?Style.CLASSIC:v;}
}
