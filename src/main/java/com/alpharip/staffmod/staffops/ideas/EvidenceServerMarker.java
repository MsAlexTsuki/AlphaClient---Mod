package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class EvidenceServerMarker {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-server-marker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;String server="Singleplayer";if(m.getCurrentServerData()!=null)server=m.getCurrentServerData().serverIP;m.fontRendererObj.drawStringWithShadow("Server marker: "+server,6,6,0xFFFFFF55);}
}