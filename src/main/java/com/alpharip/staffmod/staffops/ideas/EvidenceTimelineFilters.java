package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Holds a local event-type filter for evidence timeline review. */
public final class EvidenceTimelineFilters {
 private String filter="all";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-timeline-filters");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message==null||!e.message.startsWith("/evidencefilter "))return;String v=e.message.substring(16).trim();if(v.matches("[A-Za-z0-9_-]{1,24}"))filter=v;e.setCanceled(true);}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Timeline Filter: "+filter,6,199,0xFFB8D7FF);}
}