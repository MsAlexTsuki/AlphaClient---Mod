package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays a compact local list of names in the configured investigation group. */
public final class StaffGroupHighlight {
 private String group="";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-group-highlight");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(net.minecraftforge.client.event.ClientChatEvent e){if(e.message!=null&&e.message.startsWith("/alphagroup ")){group=e.message.substring(12).trim();e.setCanceled(true);}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled()||group.isEmpty())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.theWorld==null||mc.gameSettings.showDebugInfo)return;int y=30;mc.fontRendererObj.drawStringWithShadow("Group: "+group,6,y,0xFFFFC83D);y+=11;for(Object o:mc.theWorld.playerEntities){EntityPlayer p=(EntityPlayer)o;if(p!=mc.thePlayer&&y<110){mc.fontRendererObj.drawStringWithShadow(p.getName(),6,y,0xFFFFE8A0);y+=10;}}}
}