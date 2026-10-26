package com.alpharip.staffmod.staffops.ideas;

/** Foundation for representing the direction of received knockback. */
public class KnockbackDirectionIndicator {
 private double directionX; private double directionZ; private boolean visible;
 public void updateDirection(double x,double z){directionX=x;directionZ=z;visible=true;} public double getDirectionX(){return directionX;} public double getDirectionZ(){return directionZ;} public boolean isVisible(){return visible;} public void clear(){visible=false;directionX=0;directionZ=0;}
}
