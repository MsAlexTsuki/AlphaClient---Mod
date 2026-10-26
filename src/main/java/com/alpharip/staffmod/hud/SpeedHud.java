package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of speed-hud. */
public final class SpeedHud {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("speed-hud");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  double h=Math.sqrt(mc.thePlayer.motionX*mc.thePlayer.motionX+mc.thePlayer.motionZ*mc.thePlayer.motionZ)*20.0D; mc.fontRendererObj.drawStringWithShadow(String.format("Speed: %.2f blocks/s",h),6,306,0xFFFFFFFF);
 }
}
