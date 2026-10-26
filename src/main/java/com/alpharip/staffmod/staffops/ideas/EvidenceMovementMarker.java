package com.alpharip.staffmod.staffops.ideas;

/** Describes a movement event using caller-provided positions and a timestamp. */
public final class EvidenceMovementMarker {
 private double fromX,fromY,fromZ,toX,toY,toZ;private long timestampMillis;
 public void setMovement(double fromX,double fromY,double fromZ,double toX,double toY,double toZ,long timestampMillis){this.fromX=fromX;this.fromY=fromY;this.fromZ=fromZ;this.toX=toX;this.toY=toY;this.toZ=toZ;this.timestampMillis=timestampMillis;}
 public double getFromX(){return fromX;}public double getFromY(){return fromY;}public double getFromZ(){return fromZ;}public double getToX(){return toX;}public double getToY(){return toY;}public double getToZ(){return toZ;}public long getTimestampMillis(){return timestampMillis;}
}