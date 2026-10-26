package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Assigns a local category label to an evidence item. */
public final class EvidenceCategories {
 private String category="Uncategorized";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-categories");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(net.minecraftforge.client.event.ClientChatEvent e){if(e.message!=null&&e.message.startsWith("/evidencecategory ")){String c=e.message.substring(18).trim();if(!c.isEmpty()){category=c.length()>32?c.substring(0,32):c;e.setCanceled(true);}}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Category: "+category,6,89,0xFFB8D7FF);}
}