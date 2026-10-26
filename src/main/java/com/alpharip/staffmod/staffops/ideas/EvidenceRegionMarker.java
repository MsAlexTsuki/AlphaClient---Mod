package com.alpharip.staffmod.staffops.ideas;

/** Rectangular region metadata for marking an area in an evidence image. */
public final class EvidenceRegionMarker {
 private double x,y,width,height;private String label="";
 public void setRegion(double x,double y,double width,double height,String label){if(width<0||height<0)throw new IllegalArgumentException("Region size cannot be negative");this.x=x;this.y=y;this.width=width;this.height=height;this.label=label==null?"":label;}
 public double getX(){return x;}public double getY(){return y;}public double getWidth(){return width;}public double getHeight(){return height;}public String getLabel(){return label;}
}