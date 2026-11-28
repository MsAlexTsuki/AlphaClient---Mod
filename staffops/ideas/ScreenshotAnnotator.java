package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Screenshot annotation data. Drawing annotations onto pixels must be connected to image export. */
public class ScreenshotAnnotator {
 public static final class Annotation {public final String type,text;public final int x1,y1,x2,y2,rgb;Annotation(String t,String s,int a,int b,int c,int d,int color){type=t;text=s;x1=a;y1=b;x2=c;y2=d;rgb=color&0xFFFFFF;}}
 private final List<Annotation> items=new ArrayList<Annotation>();
 public void addText(String text,int x,int y,int rgb){items.add(new Annotation("text",text==null?"":text,x,y,x,y,rgb));}
 public void addArrow(int x1,int y1,int x2,int y2,int rgb){items.add(new Annotation("arrow","",x1,y1,x2,y2,rgb));}
 public void addRectangle(int x1,int y1,int x2,int y2,int rgb){items.add(new Annotation("rectangle","",x1,y1,x2,y2,rgb));}
 public List<Annotation> getAnnotations(){return Collections.unmodifiableList(new ArrayList<Annotation>(items));}public void clear(){items.clear();}
}