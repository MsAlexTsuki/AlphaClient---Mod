package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
public final class VelocityHud {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("velocity-hud");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  double h=Math.sqrt(mc.thePlayer.motionX*mc.thePlayer.motionX+mc.thePlayer.motionZ*mc.thePlayer.motionZ);
  mc.fontRendererObj.drawStringWithShadow(String.format("Velocity: H %.3f V %.3f",h,mc.thePlayer.motionY),6,210,0xFFFFFFFF);
 }
}
