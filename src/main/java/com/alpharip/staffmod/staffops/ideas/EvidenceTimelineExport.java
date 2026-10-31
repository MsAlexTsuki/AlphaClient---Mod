package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Prepares a local textual export summary; does not write files or send data. */
public final class EvidenceTimelineExport {
 private String summary="No timeline summary prepared";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-timeline-export");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message!=null&&e.message.equalsIgnoreCase("/evidencetimeline export")){summary="Local export requested at "+System.currentTimeMillis()+"; no file was written";e.setCanceled(true);}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Timeline Export: "+summary,6,221,0xFFB8D7FF);}
}