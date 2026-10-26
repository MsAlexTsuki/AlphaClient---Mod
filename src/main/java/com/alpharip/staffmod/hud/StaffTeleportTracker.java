package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Flags large local-player position jumps; it cannot determine whether a server teleport was legitimate. */
public final class StaffTeleportTracker {
 private double x,y,z;private boolean initialized;private String last="No large movement detected";private long at;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-teleport-tracker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onTick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null){initialized=false;return;}if(initialized){double dx=mc.thePlayer.posX-x,dy=mc.thePlayer.posY-y,dz=mc.thePlayer.posZ-z;if(dx*dx+dy*dy+dz*dz>100.0){last=String.format("Jump %.1f blocks",Math.sqrt(dx*dx+dy*dy+dz*dz));at=System.currentTimeMillis();}}x=mc.thePlayer.posX;y=mc.thePlayer.posY;z=mc.thePlayer.posZ;initialized=true;}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Teleport Tracker: "+last,6,6,0xFFFFCC55);if(at>0)mc.fontRendererObj.drawStringWithShadow(((System.currentTimeMillis()-at)/1000)+"s ago",6,18,0xFFFFFFFF);}
}