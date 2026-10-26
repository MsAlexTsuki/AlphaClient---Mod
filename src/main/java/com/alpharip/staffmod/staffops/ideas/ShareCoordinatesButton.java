package com.alpharip.staffmod.staffops.ideas;
/** Formats coordinates for chat or clipboard actions; UI action is not wired. */
public class ShareCoordinatesButton {
 public String format(int x,int y,int z){return "X: "+x+" Y: "+y+" Z: "+z;}
 public String formatCompact(int x,int y,int z){return x+", "+y+", "+z;}
}