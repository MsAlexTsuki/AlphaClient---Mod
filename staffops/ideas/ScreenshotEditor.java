package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Screenshot edit plan; image rendering/export must be connected to Minecraft's screenshot pipeline. */
public class ScreenshotEditor {
 public static final class Crop {public final int x,y,width,height;public Crop(int x,int y,int w,int h){if(x<0||y<0||w<=0||h<=0)throw new IllegalArgumentException("Invalid crop bounds");this.x=x;this.y=y;width=w;height=h;}}
 public static final class Text {public final String value;public final int x,y,rgb;public Text(String v,int x,int y,int c){if(v==null)throw new IllegalArgumentException("Text required");value=v;this.x=x;this.y=y;rgb=c&0xFFFFFF;}}
 private boolean enabled=true;private Crop crop;private final List<Text> texts=new ArrayList<Text>();
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public void setCrop(int x,int y,int w,int h){crop=new Crop(x,y,w,h);}public Crop getCrop(){return crop;}public void clearCrop(){crop=null;}
 public void addText(String t,int x,int y,int rgb){texts.add(new Text(t,x,y,rgb));}public List<Text> getTexts(){return Collections.unmodifiableList(new ArrayList<Text>(texts));}public void clearText(){texts.clear();}
}