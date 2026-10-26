package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Text/grid sketch model for a base plan; drawing canvas and file persistence are separate integration. */
public final class EnemyBaseSketchpad {
 public static final String NAME="Enemy Base Sketchpad",CATEGORY="HardcoreFactions",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final int width,height;private final char[][] grid;private final Map<String,String> annotations=new LinkedHashMap<String,String>();
 public EnemyBaseSketchpad(){this(32,32);}public EnemyBaseSketchpad(int w,int h){if(w<1||h<1||w>256||h>256)throw new IllegalArgumentException("dimensions must be 1..256");width=w;height=h;grid=new char[h][w];for(char[] row:grid)Arrays.fill(row,' ');}
 public synchronized void mark(int x,int y,char symbol){check(x,y);grid[y][x]=symbol;}
 public synchronized void annotate(String key,String note){if(note==null||note.trim().isEmpty())annotations.remove(text(key));else annotations.put(text(key),note.trim());}
 public synchronized String render(){StringBuilder b=new StringBuilder();for(char[] row:grid)b.append(row).append('\n');return b.toString();}
 public synchronized Map<String,String> annotations(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(annotations));}
 private void check(int x,int y){if(x<0||y<0||x>=width||y>=height)throw new IndexOutOfBoundsException();}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("key required");return s.trim();}
}