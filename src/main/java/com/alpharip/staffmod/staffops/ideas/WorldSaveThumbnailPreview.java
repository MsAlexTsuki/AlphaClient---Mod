package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Maps world-save folders to thumbnail paths; thumbnail generation and save-menu UI are not connected. */
public class WorldSaveThumbnailPreview {
 private final Map<String,String> thumbnails=new LinkedHashMap<String,String>();
 public void setThumbnail(String saveFolder,String imagePath){if(saveFolder==null||saveFolder.trim().isEmpty())throw new IllegalArgumentException("Save folder required");if(imagePath==null||imagePath.trim().isEmpty())thumbnails.remove(saveFolder);else thumbnails.put(saveFolder,imagePath);}
 public String getThumbnail(String saveFolder){return thumbnails.get(saveFolder);}
 public Map<String,String> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(thumbnails));}
}