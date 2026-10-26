package com.alpharip.staffmod.staffops.ideas;
import java.text.SimpleDateFormat;import java.util.*;
/** Expands safe screenshot filename tokens: {date}, {time}, {server}, {world}, {x}, {y}, {z}. */
public class ScreenshotNameTemplate {
 private String template="{date}_{time}_{server}";
 public String getTemplate(){return template;}public void setTemplate(String t){if(t==null||t.trim().isEmpty())throw new IllegalArgumentException("Template required");template=t.trim();}
 public String format(Date date,String server,String world,int x,int y,int z){Date d=date==null?new Date():date;String out=template.replace("{date}",new SimpleDateFormat("yyyy-MM-dd").format(d)).replace("{time}",new SimpleDateFormat("HH-mm-ss").format(d)).replace("{server}",safe(server)).replace("{world}",safe(world)).replace("{x}",String.valueOf(x)).replace("{y}",String.valueOf(y)).replace("{z}",String.valueOf(z));return out.replaceAll("[^A-Za-z0-9._-]","_");}
 private static String safe(String s){return s==null||s.trim().isEmpty()?"singleplayer":s.trim().replaceAll("[^A-Za-z0-9._-]","_");}
}