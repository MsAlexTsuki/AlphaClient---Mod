package com.alpharip.staffmod.staffops.ideas;
/** Now-playing badge data; Spotify API/authentication and HUD rendering are not connected. */
public class SpotifyNowPlayingBadge {
 private boolean enabled=true;private String track="",artist="",album="",url="";private long updatedAt;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}private static String clean(String s){return s==null?"":s.trim();}
 public void setTrack(String t,String a,String al,String u,long time){track=clean(t);artist=clean(a);album=clean(al);url=clean(u);updatedAt=time;}
 public String getTrack(){return track;}public String getArtist(){return artist;}public String getAlbum(){return album;}public String getExternalUrl(){return url;}public long getUpdatedAt(){return updatedAt;}
 public String getDisplayText(){return track.isEmpty()?"":artist.isEmpty()?track:track+" — "+artist;}public void clear(){track=artist=album=url="";updatedAt=0;}
}