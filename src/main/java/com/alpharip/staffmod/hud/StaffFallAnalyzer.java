package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays the local player's current accumulated fall distance. */
public final class StaffFallAnalyzer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-fall-analyzer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  mc.fontRendererObj.drawStringWithShadow("Fall Analyzer",6,6,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow(String.format("Current fall distance: %.2f blocks",mc.thePlayer.fallDistance),6,18,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("On ground: "+mc.thePlayer.onGround,6,30,0xFFFFFFFF);
 }
}