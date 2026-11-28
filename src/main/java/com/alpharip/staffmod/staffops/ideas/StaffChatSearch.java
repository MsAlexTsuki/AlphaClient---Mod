package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayDeque;import java.util.Deque;
/** Searches newly received client chat lines using a term entered with /alphachatsearch <term>. */
public final class StaffChatSearch {
 private String term="";private final Deque<String> matches=new ArrayDeque<String>();
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-chat-search");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onInput(ClientChatEvent e){if(!enabled()||e.message==null||!e.message.startsWith("/alphachatsearch "))return;term=e.message.substring(17).trim();matches.clear();e.setCanceled(true);}
 @SubscribeEvent public void onChat(ClientChatReceivedEvent e){if(!enabled()||term.isEmpty()||e.message==null)return;String line=e.message.getUnformattedText();if(line.toLowerCase().contains(term.toLowerCase())){matches.addFirst(line);while(matches.size()>4)matches.removeLast();}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;int y=6;mc.fontRendererObj.drawStringWithShadow("Chat Search: "+(term.isEmpty()?"type /alphachatsearch <term>":term),6,y,0xFF55DDFF);for(String s:matches){y+=12;mc.fontRendererObj.drawStringWithShadow(s,6,y,0xFFFFFFFF);}}
}