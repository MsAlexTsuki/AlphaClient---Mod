package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Alerts on a locally configured keyword, set with /alphakeyword <term>. */
public final class StaffKeywordAlert {
 private String term="";private String latest="";private long at;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-keyword-alert");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onInput(ClientChatEvent e){if(!enabled()||e.message==null||!e.message.startsWith("/alphakeyword "))return;term=e.message.substring(14).trim();e.setCanceled(true);}
 @SubscribeEvent public void onChat(ClientChatReceivedEvent e){if(!enabled()||term.isEmpty()||e.message==null)return;String line=e.message.getUnformattedText();if(line.toLowerCase().contains(term.toLowerCase())){latest=line.length()>90?line.substring(0,90)+"…":line;at=System.currentTimeMillis();}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Keyword Alert ["+(term.isEmpty()?"not set":term)+"]",6,6,0xFFFF5555);if(at>0&&System.currentTimeMillis()-at<5000)mc.fontRendererObj.drawStringWithShadow(latest,6,18,0xFFFFFFFF);}
}