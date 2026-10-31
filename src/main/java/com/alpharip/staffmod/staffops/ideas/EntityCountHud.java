package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of entity-count-hud. */
public final class EntityCountHud {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("entity-count-hud");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  int count=mc.theWorld.getLoadedEntityList().size(); mc.fontRendererObj.drawStringWithShadow("Loaded entities: "+count,6,342,0xFFFFFFFF);
 }
}
