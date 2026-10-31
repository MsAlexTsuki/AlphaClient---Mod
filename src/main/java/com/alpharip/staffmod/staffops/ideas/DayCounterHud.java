package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of day-counter. */
public final class DayCounter {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("day-counter");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  long day=Math.max(0L,mc.theWorld.getWorldTime()/24000L); mc.fontRendererObj.drawStringWithShadow("World day: "+(day+1L),6,330,0xFFFFFFFF);
 }
}
