package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Data model for a user-drawn annotation stroke; rendering is not included. */
public final class EvidenceDrawingTools {
 private final List<Point> points=new ArrayList<Point>(); private String label="";
 public void setLabel(String label){this.label=label==null?"":label;} public String getLabel(){return label;} public void addPoint(double x,double y){points.add(new Point(x,y));}
 public List<Point> getPoints(){return Collections.unmodifiableList(new ArrayList<Point>(points));} public void clear(){points.clear();}
 public static final class Point {private final double x,y;public Point(double x,double y){this.x=x;this.y=y;}public double getX(){return x;}public double getY(){return y;}}
}