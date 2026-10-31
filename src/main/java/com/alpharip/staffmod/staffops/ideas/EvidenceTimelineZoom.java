package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Adjusts the displayed time-window scale for a local evidence timeline. */
public final class EvidenceTimelineZoom {
 private int windowMinutes=30;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-timeline-zoom");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message==null||!e.message.startsWith("/evidencetimewindow "))return;try{int n=Integer.parseInt(e.message.substring(20).trim());if(n>=1&&n<=1440)windowMinutes=n;}catch(Exception ignored){}e.setCanceled(true);}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Timeline Window: "+windowMinutes+" min",6,188,0xFFB8D7FF);}
}