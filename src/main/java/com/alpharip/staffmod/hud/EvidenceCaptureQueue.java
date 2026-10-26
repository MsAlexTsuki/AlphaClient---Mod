package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Local evidence-capture queue marker for staff review; does not upload evidence. */
public final class EvidenceCaptureQueue {
 private int queued; private String latest="No evidence queued";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-capture-queue");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(net.minecraftforge.client.event.ClientChatEvent e){if(e.message!=null&&e.message.equalsIgnoreCase("/evidencequeue add")){queued++;latest="Evidence item "+queued+" queued locally";e.setCanceled(true);}else if(e.message!=null&&e.message.equalsIgnoreCase("/evidencequeue clear")){queued=0;latest="Queue cleared locally";e.setCanceled(true);}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Queue: "+queued,6,54,0xFFFFC83D);mc.fontRendererObj.drawStringWithShadow(latest,6,65,0xFFFFFFFF);}
}