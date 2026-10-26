package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays two manually entered evidence notes side-by-side as a compact comparison. */
public final class EvidenceSideBySide {
 private String left="Left: empty",right="Right: empty";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-side-by-side");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message==null)return;if(e.message.startsWith("/evidenceleft ")){left=clip(e.message.substring(14));e.setCanceled(true);}else if(e.message.startsWith("/evidenceright ")){right=clip(e.message.substring(15));e.setCanceled(true);}}
 private String clip(String s){s=s.trim();return s.length()>28?s.substring(0,28):s;}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow(left+" | "+right,6,177,0xFFFFFFFF);}
}