package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffLocalClock {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-local-clock");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;m.fontRendererObj.drawStringWithShadow("Local Clock: "+new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()),6,6,-1);}
}