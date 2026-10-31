package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Highlights locally listed friends by name; add names with /alphafriend <name>. */
public final class StaffFriendHighlight {
 private final java.util.Set<String> friends=new java.util.HashSet<String>();
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-friend-highlight");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(net.minecraftforge.client.event.ClientChatEvent e){if(e.message!=null&&e.message.startsWith("/alphafriend ")){friends.add(e.message.substring(13).trim().toLowerCase());e.setCanceled(true);}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.theWorld==null||mc.gameSettings.showDebugInfo)return;int y=30;for(Object o:mc.theWorld.playerEntities){EntityPlayer p=(EntityPlayer)o;if(friends.contains(p.getName().toLowerCase())){mc.fontRendererObj.drawStringWithShadow("[Friend] "+p.getName(),6,y,0xFF55FF55);y+=10;}}}
}