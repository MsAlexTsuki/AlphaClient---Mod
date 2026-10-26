package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Captures a small local context note on request; no screenshots or uploads are made. */
public final class EvidenceContextCapture {
 private String context="No context captured";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-context-capture");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message!=null&&e.message.equalsIgnoreCase("/evidencecontext capture")){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer!=null&&mc.theWorld!=null)context="Player="+mc.thePlayer.getName()+", dimension="+mc.thePlayer.dimension+", xyz="+(int)mc.thePlayer.posX+","+(int)mc.thePlayer.posY+","+(int)mc.thePlayer.posZ+", time="+System.currentTimeMillis();else context="World unavailable";e.setCanceled(true);}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Context: "+context,6,144,0xFFB8D7FF);}
}