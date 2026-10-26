package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side staff utility; requires later module/event registration. */
public final class StaffFavorites {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-favorites");return m!=null&&m.isEnabled()&&m.isUnlocked();}
private final String[] favorites={"Player Inspector","Block Inspector","Chat Search","Evidence Capture"};
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;m.fontRendererObj.drawStringWithShadow("Staff Favorites — default suggestions",6,6,-1);for(int i=0;i<favorites.length;i++)m.fontRendererObj.drawStringWithShadow("• "+favorites[i],6,18+i*10,0xFFFFFF55);}
}