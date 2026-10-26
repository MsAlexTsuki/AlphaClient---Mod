package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side evidence utility; requires later module/event registration. */
public final class EvidenceLocationMarker {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-location-marker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;m.fontRendererObj.drawStringWithShadow(String.format("Evidence Location: %.3f, %.3f, %.3f",m.thePlayer.posX,m.thePlayer.posY,m.thePlayer.posZ),6,6,0xFFFFFF55);}
}