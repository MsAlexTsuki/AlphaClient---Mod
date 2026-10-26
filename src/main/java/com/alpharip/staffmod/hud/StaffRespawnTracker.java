package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Detects local-player respawns by observing a dead-to-alive transition. */
public final class StaffRespawnTracker {
 private boolean wasDead;private long lastRespawn;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-respawn-tracker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onTick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null){wasDead=false;return;}boolean dead=mc.thePlayer.isDead;if(wasDead&&!dead)lastRespawn=System.currentTimeMillis();wasDead=dead;}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled()||lastRespawn==0)return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Last Respawn: "+((System.currentTimeMillis()-lastRespawn)/1000)+"s ago",6,6,0xFF55FF55);}
}