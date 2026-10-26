package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores timestamps for user-marked moments; recorder integration is not connected. */
public class BestMomentMarker {
 public static final class Marker {public final long timestamp;public final String label;Marker(long t,String l){timestamp=t;label=l;}}
 private final List<Marker> markers=new ArrayList<Marker>();
 public void mark(long timestamp,String label){markers.add(new Marker(timestamp,label==null||label.trim().isEmpty()?"Best moment":label.trim()));}
 public List<Marker> getMarkers(){return Collections.unmodifiableList(new ArrayList<Marker>(markers));}
 public boolean removeAt(int index){if(index<0||index>=markers.size())return false;markers.remove(index);return true;}public void clear(){markers.clear();}
}