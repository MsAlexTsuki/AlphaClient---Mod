package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Assigns categories and RGB colors to named waypoints. */
public class WaypointCategoriesColors {private final Map<String,String> categories=new LinkedHashMap<String,String>();private final Map<String,Integer> colors=new LinkedHashMap<String,Integer>();
 public void set(String waypoint,String category,int rgb){String id=req(waypoint);if(rgb<0||rgb>0xFFFFFF)throw new IllegalArgumentException("rgb");categories.put(id,req(category));colors.put(id,rgb);}
 public String getCategory(String waypoint){return categories.get(req(waypoint));}public Integer getColor(String waypoint){return colors.get(req(waypoint));}
 public Map<String,String> getCategories(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(categories));}private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}