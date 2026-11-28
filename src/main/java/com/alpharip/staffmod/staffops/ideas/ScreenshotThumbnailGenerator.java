package com.alpharip.staffmod.staffops.ideas;

/** Foundation for keeping thumbnail metadata for screenshots. */
public class ScreenshotThumbnailGenerator {
 private final java.util.Map<String,String> thumbnails=new java.util.LinkedHashMap<String,String>();
 public void register(String screenshotPath,String thumbnailPath){if(screenshotPath!=null&&!screenshotPath.trim().isEmpty()&&thumbnailPath!=null&&!thumbnailPath.trim().isEmpty())thumbnails.put(screenshotPath,thumbnailPath);}
 public String getThumbnail(String screenshotPath){return thumbnails.get(screenshotPath);}
 public boolean remove(String screenshotPath){return thumbnails.remove(screenshotPath)!=null;}
 public java.util.Map<String,String> getThumbnails(){return java.util.Collections.unmodifiableMap(thumbnails);}
}
