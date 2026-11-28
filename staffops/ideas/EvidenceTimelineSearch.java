package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Keeps a local search term for evidence timeline review. */
public final class EvidenceTimelineSearch {
 private String query="";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-timeline-search");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message==null||!e.message.startsWith("/evidencesearch "))return;query=e.message.substring(16).trim();if(query.length()>40)query=query.substring(0,40);e.setCanceled(true);}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Search: "+(query.isEmpty()?"(no query)":query),6,210,0xFFFFFFFF);}
}