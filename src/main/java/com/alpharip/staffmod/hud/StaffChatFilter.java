package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Optional client-side chat filter controlled with /alphachatfilter <term>; matching incoming lines are hidden locally. */
public final class StaffChatFilter {
 private String term="";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-chat-filter");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onInput(ClientChatEvent e){if(!enabled()||e.message==null||!e.message.startsWith("/alphachatfilter "))return;term=e.message.substring(17).trim();e.setCanceled(true);}
 @SubscribeEvent public void onChat(ClientChatReceivedEvent e){if(!enabled()||term.isEmpty()||e.message==null)return;if(e.message.getUnformattedText().toLowerCase().contains(term.toLowerCase()))e.setCanceled(true);}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Chat Filter: "+(term.isEmpty()?"off":term),6,6,0xFFFFAA55);}
}