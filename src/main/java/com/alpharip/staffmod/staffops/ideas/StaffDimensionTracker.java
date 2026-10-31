package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Tracks dimension changes observed for the local player. */
public final class StaffDimensionTracker {
 private int dimension=Integer.MIN_VALUE;private String message="Waiting for dimension";private long changedAt;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-dimension-tracker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onTick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null)return;int current=mc.thePlayer.dimension;if(dimension!=Integer.MIN_VALUE&&current!=dimension){message=dimension+" -> "+current;changedAt=System.currentTimeMillis();}dimension=current;}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Dimension: "+mc.thePlayer.dimension,6,6,0xFF55DDFF);if(changedAt>0)mc.fontRendererObj.drawStringWithShadow("Last change: "+message+" ("+((System.currentTimeMillis()-changedAt)/1000)+"s)",6,18,0xFFFFFFFF);}
}