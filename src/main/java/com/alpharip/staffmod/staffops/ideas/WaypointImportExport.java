package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** TSV import/export for waypoint data; caller persists the returned records. */
public class WaypointImportExport {
 public static final class Waypoint {public final String name,world;public final int x,y,z;public Waypoint(String n,String w,int x,int y,int z){if(blank(n)||blank(w))throw new IllegalArgumentException();name=n;world=w;this.x=x;this.y=y;this.z=z;}}
 public String exportWaypoints(List<Waypoint> list){StringBuilder b=new StringBuilder("name\tworld\tx\ty\tz\n");if(list!=null)for(Waypoint w:list)if(w!=null)b.append(esc(w.name)).append('\t').append(esc(w.world)).append('\t').append(w.x).append('\t').append(w.y).append('\t').append(w.z).append('\n');return b.toString();}
 public List<Waypoint> importWaypoints(String text){List<Waypoint> out=new ArrayList<Waypoint>();if(text==null)return out;String[] lines=text.split("\\r?\\n");for(int i=1;i<lines.length;i++){if(lines[i].trim().isEmpty())continue;String[] p=lines[i].split("\\t",-1);if(p.length!=5)throw new IllegalArgumentException("Invalid waypoint line "+(i+1));out.add(new Waypoint(unesc(p[0]),unesc(p[1]),Integer.parseInt(p[2]),Integer.parseInt(p[3]),Integer.parseInt(p[4])));}return Collections.unmodifiableList(out);}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}private static String esc(String s){return s.replace("\\","\\\\").replace("\t","\\t").replace("\n","\\n");}private static String unesc(String s){return s.replace("\\n","\n").replace("\\t","\t").replace("\\\\","\\");}
}