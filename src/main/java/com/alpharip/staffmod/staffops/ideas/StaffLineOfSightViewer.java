package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffLineOfSightViewer {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-line-of-sight-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
private String last="No entity targeted";
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;if(m.objectMouseOver!=null&&m.objectMouseOver.entityHit!=null){net.minecraft.entity.Entity t=m.objectMouseOver.entityHit;last=t.getName()+String.format(" | %.2f blocks",m.thePlayer.getDistanceToEntity(t));}m.fontRendererObj.drawStringWithShadow("Line-of-Sight Viewer",6,6,-1);m.fontRendererObj.drawStringWithShadow("Crosshair ray target: "+last,6,18,0xFFCCCCCC);m.fontRendererObj.drawStringWithShadow("Client raycast target; not proof of full occlusion",6,30,0xFFAAAAAA);}
}