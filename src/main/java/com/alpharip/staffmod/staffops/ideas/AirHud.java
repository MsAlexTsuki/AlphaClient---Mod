package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of air-hud. */
public final class AirHud {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("air-hud");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  if(mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.water)) mc.fontRendererObj.drawStringWithShadow("Air: "+mc.thePlayer.getAir()+" ticks",6,294,0xFFFFFFFF);
 }
}
