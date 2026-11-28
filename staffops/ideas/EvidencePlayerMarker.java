package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side evidence utility; requires later module/event registration. */
public final class EvidencePlayerMarker {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-player-marker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;net.minecraft.entity.Entity t=m.objectMouseOver==null?null:m.objectMouseOver.entityHit;if(!(t instanceof net.minecraft.entity.player.EntityPlayer)){m.fontRendererObj.drawStringWithShadow("Evidence Player Marker: no player targeted",6,6,-1);return;}m.fontRendererObj.drawStringWithShadow(String.format("Evidence Player: %s | %.2f blocks",t.getName(),m.thePlayer.getDistanceToEntity(t)),6,6,0xFFFFFF55);}
}