package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of hunger-warning. */
public final class HungerWarning {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("hunger-warning");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  int food=mc.thePlayer.getFoodStats().getFoodLevel(); if(food<=6) mc.fontRendererObj.drawStringWithShadow("WARNING: low food ("+food+"/20)",6,366,0xFFFF5555);
 }
}
