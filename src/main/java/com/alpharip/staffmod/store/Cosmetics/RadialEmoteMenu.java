package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Eight-sector emote wheel model; screen rendering and emote dispatch are not connected. */
public class RadialEmoteMenu {
 private boolean enabled=true;private final String[] sectors=new String[8];public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public void setSector(int i,String e){check(i);sectors[i]=e==null||e.trim().isEmpty()?null:e.trim();}public String getSector(int i){check(i);return sectors[i];}
 public String selectAngle(double degrees){int i=((int)Math.floor((((degrees%360)+360)%360+22.5)/45))%8;return sectors[i];}
 public Map<Integer,String> snapshot(){Map<Integer,String> m=new LinkedHashMap<Integer,String>();for(int i=0;i<8;i++)if(sectors[i]!=null)m.put(i,sectors[i]);return Collections.unmodifiableMap(m);}private static void check(int i){if(i<0||i>=8)throw new IllegalArgumentException("Sector must be 0-7");}
}