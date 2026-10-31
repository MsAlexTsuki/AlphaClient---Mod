package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Stores a local priority label for the current evidence task. */
public final class EvidencePriority {
 private String priority="Normal";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-priority");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(net.minecraftforge.client.event.ClientChatEvent e){if(e.message!=null&&e.message.startsWith("/evidencepriority ")){String p=e.message.substring(18).trim();if(p.equalsIgnoreCase("low")||p.equalsIgnoreCase("normal")||p.equalsIgnoreCase("high")||p.equalsIgnoreCase("urgent")){priority=p.substring(0,1).toUpperCase()+p.substring(1).toLowerCase();e.setCanceled(true);}}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Priority: "+priority,6,78,0xFFFFC83D);}
}