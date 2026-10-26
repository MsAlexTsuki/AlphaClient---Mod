package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays the latest chat line received by this client as context. */
public final class StaffChatContext {
 private String latest="No chat received";private long received;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-chat-context");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatReceivedEvent e){if(!enabled()||e.message==null)return;latest=e.message.getUnformattedText();if(latest.length()>100)latest=latest.substring(0,100)+"…";received=System.currentTimeMillis();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled()||received==0)return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Chat Context: "+latest,6,6,0xFFFFFFFF);}
}