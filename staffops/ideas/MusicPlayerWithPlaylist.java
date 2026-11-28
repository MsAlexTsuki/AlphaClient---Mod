package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Playlist queue and selection state; actual audio decoding/playback is not connected. */
public class MusicPlayerWithPlaylist {private final List<String> tracks=new ArrayList<String>();private int index=-1;private boolean shuffle,repeat;
 public void add(String track){if(track==null||track.trim().isEmpty())throw new IllegalArgumentException();tracks.add(track.trim());if(index<0)index=0;}
 public String current(){return index<0||index>=tracks.size()?null:tracks.get(index);}public List<String> getPlaylist(){return Collections.unmodifiableList(new ArrayList<String>(tracks));}
 public String next(){if(tracks.isEmpty())return null;if(shuffle)index=new Random().nextInt(tracks.size());else if(index+1<tracks.size())index++;else if(repeat)index=0;else return null;return current();}
 public void setShuffle(boolean v){shuffle=v;}public void setRepeat(boolean v){repeat=v;}public void remove(int i){if(i<0||i>=tracks.size())return;tracks.remove(i);if(tracks.isEmpty())index=-1;else if(index>=tracks.size())index=tracks.size()-1;}
}