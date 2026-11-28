package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Labeled coordinate notes; persistence and in-game editor are not connected. */
public class CoordinateNotesPad {
 public static final class Note {public final String label,world,text;public final int x,y,z;Note(String l,String w,int x,int y,int z,String t){label=l;world=w;this.x=x;this.y=y;this.z=z;text=t;}}
 private boolean enabled=true;private final LinkedHashMap<String,Note> notes=new LinkedHashMap<String,Note>();public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public void put(String label,String world,int x,int y,int z,String text){if(label==null||label.trim().isEmpty())throw new IllegalArgumentException("Label required");String w=world==null?"":world.trim();notes.put(label.trim(),new Note(label.trim(),w,x,y,z,text==null?"":text));}
 public Note get(String label){return notes.get(label);}public boolean remove(String label){return notes.remove(label)!=null;}
 public List<Note> listForWorld(String world){List<Note> out=new ArrayList<Note>();String w=world==null?"":world.trim();for(Note n:notes.values())if(n.world.equals(w))out.add(n);return Collections.unmodifiableList(out);}
}