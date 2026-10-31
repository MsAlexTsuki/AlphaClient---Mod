package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Maintains a local evidence label for review and organization. */
public final class EvidenceLabels {
 private String label="Unlabeled";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-labels");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(net.minecraftforge.client.event.ClientChatEvent e){if(e.message!=null&&e.message.startsWith("/evidencelabel ")){String v=e.message.substring(15).trim();if(!v.isEmpty()){label=v.length()>40?v.substring(0,40):v;e.setCanceled(true);}}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Label: "+label,6,100,0xFFB8D7FF);}
}