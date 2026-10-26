package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Alerts locally when received chat contains the current player's name. */
public final class StaffMentionAlert {
 private String latest="";private long at;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-mention-alert");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatReceivedEvent e){if(!enabled()||e.message==null)return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null)return;String line=e.message.getUnformattedText();if(line.toLowerCase().contains(mc.thePlayer.getName().toLowerCase())){latest=line.length()>90?line.substring(0,90)+"…":line;at=System.currentTimeMillis();}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled()||at==0||System.currentTimeMillis()-at>5000)return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Mention: "+latest,6,6,0xFFFFFF55);}
}