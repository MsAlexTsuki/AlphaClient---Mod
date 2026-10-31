package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side evidence utility; requires later module/event registration. */
public final class EvidenceTimestampOverlay {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-timestamp-overlay");return m!=null&&m.isEnabled()&&m.isUnlocked();}
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;String stamp=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());m.fontRendererObj.drawStringWithShadow("Evidence timestamp: "+stamp,6,6,-1);}
}