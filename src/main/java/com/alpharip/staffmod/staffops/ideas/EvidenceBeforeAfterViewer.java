package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Stores two short local text notes for before/after comparison. */
public final class EvidenceBeforeAfterViewer {
 private String before="(empty)",after="(empty)";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-before-after-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message==null)return;if(e.message.startsWith("/evidencebefore ")){before=clip(e.message.substring(15));e.setCanceled(true);}else if(e.message.startsWith("/evidenceafter ")){after=clip(e.message.substring(14));e.setCanceled(true);}}
 private String clip(String s){s=s.trim();return s.length()>55?s.substring(0,55):s;}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Before: "+before,6,155,0xFFFFFFFF);mc.fontRendererObj.drawStringWithShadow("After: "+after,6,166,0xFFB8D7FF);}
}