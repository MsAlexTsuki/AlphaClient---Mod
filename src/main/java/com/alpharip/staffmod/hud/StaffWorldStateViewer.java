package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffWorldStateViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-world-state-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.theWorld==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;net.minecraft.util.BlockPos p=m.thePlayer.getPosition();m.fontRendererObj.drawStringWithShadow("World State",6,6,-1);m.fontRendererObj.drawStringWithShadow("Biome: "+m.theWorld.getBiomeGenForCoords(p).biomeName+" | Dimension: "+m.thePlayer.dimension,6,18,0xFFCCCCCC);m.fontRendererObj.drawStringWithShadow("World time: "+m.theWorld.getWorldTime()+" | Rain: "+m.theWorld.isRaining(),6,30,0xFFCCCCCC);}
}