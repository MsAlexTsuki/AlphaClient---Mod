package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of experience-hud. */
public final class ExperienceHud {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("experience-hud");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  mc.fontRendererObj.drawStringWithShadow(String.format("XP: Lv %d (%.0f%%)",mc.thePlayer.experienceLevel,mc.thePlayer.experience*100.0F),6,282,0xFFFFFFFF);
 }
}
