package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side staff utility; requires later module/event registration. */
public final class StaffUtilityWheel {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-utility-wheel");return m!=null&&m.isEnabled()&&m.isUnlocked();}
private final String[] tools={"Player Review","World Review","Chat Review","Session Review"};
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;m.fontRendererObj.drawStringWithShadow("Staff Utility Wheel — quick reference",6,6,-1);for(int i=0;i<tools.length;i++)m.fontRendererObj.drawStringWithShadow((i+1)+". "+tools[i],6,18+i*10,0xFFCCCCCC);}
}