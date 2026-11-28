package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Records a local integrity-review status; it does not authenticate remote evidence. */
public final class EvidenceIntegrityMonitor {
 private String status="Not reviewed";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-integrity-monitor");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message!=null&&e.message.equalsIgnoreCase("/evidenceintegrity review")){status="Manual review requested";e.setCanceled(true);}else if(e.message!=null&&e.message.equalsIgnoreCase("/evidenceintegrity clear")){status="Not reviewed";e.setCanceled(true);}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Integrity: "+status,6,133,0xFFFFC83D);}
}