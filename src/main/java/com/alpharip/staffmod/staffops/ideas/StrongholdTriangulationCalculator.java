package com.alpharip.staffmod.staffops.ideas;

/** Foundation for recording two bearing observations for stronghold triangulation. */
public class StrongholdTriangulationCalculator {
 private double firstX,firstZ,firstBearing,secondX,secondZ,secondBearing; private boolean firstSet,secondSet;
 public void setFirstObservation(double x,double z,double bearing){firstX=x;firstZ=z;firstBearing=bearing;firstSet=true;}
 public void setSecondObservation(double x,double z,double bearing){secondX=x;secondZ=z;secondBearing=bearing;secondSet=true;}
 public boolean hasBothObservations(){return firstSet&&secondSet;}
 public double getFirstBearing(){return firstBearing;} public double getSecondBearing(){return secondBearing;}
 public double getBaselineDistance(){double x=secondX-firstX,z=secondZ-firstZ;return Math.sqrt(x*x+z*z);}
}
