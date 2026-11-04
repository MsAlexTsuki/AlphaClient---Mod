package com.alpharip.staffmod.waypoints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.WorldInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
/** Best-effort persistent identity; 1.8.9 exposes no universal remote world UUID. */
public final class ServerWaypointIntegration {
 private ServerWaypointIntegration() {}
 public static String currentServerKey(Minecraft mc) {
  if(mc==null)return "singleplayer";
  ServerData s=mc.getCurrentServerData();
  if(s!=null&&s.serverIP!=null&&!s.serverIP.trim().isEmpty())return "server:"+s.serverIP.trim().toLowerCase(Locale.ROOT);
  if(mc.theWorld!=null&&mc.theWorld.getWorldInfo()!=null){String w=mc.theWorld.getWorldInfo().getWorldName();if(w!=null&&!w.trim().isEmpty())return "world:"+w.trim().toLowerCase(Locale.ROOT);}
  return "singleplayer";
 }
 public static String currentWorldKey(Minecraft mc) {
  if(mc==null||mc.theWorld==null||mc.thePlayer==null)return "";
  World w=mc.theWorld; WorldInfo info=w.getWorldInfo(); if(info==null)return "";
  WorldBorder border=w.getWorldBorder();
  String s=currentServerKey(mc)+"|dim="+mc.thePlayer.dimension+"|name="+safe(info.getWorldName())
   +"|spawn="+info.getSpawnX()+","+info.getSpawnY()+","+info.getSpawnZ();
  if(border!=null)s+="|border="+border.getCenterX()+","+border.getCenterZ()+","+border.getDiameter();
  try { byte[] bytes=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
   StringBuilder out=new StringBuilder();for(byte v:bytes)out.append(String.format(Locale.ROOT,"%02x",v&255));return "world-v2:"+out;
  } catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
 }
 public static boolean belongsToCurrentWorld(StaffWaypoint p,Minecraft mc) {
  if(p==null||mc==null||mc.thePlayer==null)return false;
  String key=currentWorldKey(mc);
  return !key.isEmpty()&&key.equals(p.getWorldKey())&&currentServerKey(mc).equals(p.getServerKey())&&mc.thePlayer.dimension==p.getDimensionId();
 }
 private static String safe(String s){return s==null?"":s.trim().toLowerCase(Locale.ROOT);}
}