package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side staff utility; requires later module/event registration. */
public final class StaffPresets {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-presets");return m!=null&&m.isEnabled()&&m.isUnlocked();}
private final String[] presets={"Investigation","World Inspection","Chat Review","Combat Review"};
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;m.fontRendererObj.drawStringWithShadow("Staff Presets — available preset concepts",6,6,-1);for(int i=0;i<presets.length;i++)m.fontRendererObj.drawStringWithShadow((i+1)+". "+presets[i],6,18+i*10,0xFFCCCCCC);}
}