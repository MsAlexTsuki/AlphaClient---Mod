package com.alpharip.staffmod.waypoints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
/** Best-effort persistent identity; 1.8.9 exposes no universal remote world UUID. */
public final class ServerWaypointIntegration {
 private static World cachedWorld;
 private static String cachedWorldKey = "";
 private ServerWaypointIntegration() {}
 public static String currentServerKey(Minecraft mc) {
  if(mc==null)return "singleplayer";
  ServerData s=mc.getCurrentServerData();
  if(s!=null&&s.serverIP!=null&&!s.serverIP.trim().isEmpty())return "server:"+s.serverIP.trim().toLowerCase(Locale.ROOT);
  if(mc.theWorld!=null&&mc.theWorld.getWorldInfo()!=null){String w=mc.theWorld.getWorldInfo().getWorldName();if(w!=null&&!w.trim().isEmpty())return "world:"+w.trim().toLowerCase(Locale.ROOT);}
  return "singleplayer";
 }
 public static synchronized String currentWorldKey(Minecraft mc) {
  if(mc==null||mc.theWorld==null||mc.thePlayer==null)return "";
  World w=mc.theWorld;
  if(w==cachedWorld&&!cachedWorldKey.isEmpty())return cachedWorldKey;
  WorldInfo info=w.getWorldInfo(); if(info==null)return "";
  int sx=info.getSpawnX(), sz=info.getSpawnZ();
  String s=currentServerKey(mc)+"|dim="+mc.thePlayer.dimension+"|name="+safe(info.getWorldName())
   +"|spawn="+sx+","+info.getSpawnY()+","+sz;
  // A fixed terrain signature separates maps that reuse the same server, dimension and spawn metadata.
  int[] offsets={-8,0,8};
  for(int dx:offsets) for(int dz:offsets) {
   BlockPos column=new BlockPos(sx+dx,0,sz+dz);
   if(!w.isBlockLoaded(column))return "";
   BlockPos top=w.getHeight(column);
   BlockPos surface=new BlockPos(sx+dx,top.getY()-1,sz+dz);
   BlockPos below=new BlockPos(sx+dx,top.getY()-2,sz+dz);
   if(!w.isBlockLoaded(surface)||!w.isBlockLoaded(below))return "";
   s+="|"+Block.getIdFromBlock(w.getBlockState(surface).getBlock())+":"
      +w.getBlockState(surface).getBlock().getMetaFromState(w.getBlockState(surface))
      +","+Block.getIdFromBlock(w.getBlockState(below).getBlock())+":"
      +w.getBlockState(below).getBlock().getMetaFromState(w.getBlockState(below));
  }
  try { byte[] bytes=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
   StringBuilder out=new StringBuilder();for(byte v:bytes)out.append(String.format(Locale.ROOT,"%02x",v&255));
   cachedWorld=w; cachedWorldKey="world-v2:"+out; return cachedWorldKey;
  } catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
 }
 public static boolean belongsToCurrentWorld(StaffWaypoint p,Minecraft mc) {
  if(p==null||mc==null||mc.thePlayer==null)return false;
  String key=currentWorldKey(mc);
  return !key.isEmpty()&&key.equals(p.getWorldKey())&&currentServerKey(mc).equals(p.getServerKey())&&mc.thePlayer.dimension==p.getDimensionId();
 }
 private static String safe(String s){return s==null?"":s.trim().toLowerCase(Locale.ROOT);}
}