package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of health-hud. */
public final class HealthHud {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("health-hud");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  mc.fontRendererObj.drawStringWithShadow(String.format("Health: %.1f/%.1f",mc.thePlayer.getHealth(),mc.thePlayer.getMaxHealth()),6,270,0xFFFFFFFF);
 }
}
