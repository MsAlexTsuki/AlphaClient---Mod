package com.alpharip.staffmod.staffops.ideas;

/** Foundation for converting overworld and Nether coordinates. */
public class NetherCoordinateConversion {
 private static final double SCALE=8.0;
 public static double overworldToNether(double coordinate){return coordinate/SCALE;} public static double netherToOverworld(double coordinate){return coordinate*SCALE;}
}
