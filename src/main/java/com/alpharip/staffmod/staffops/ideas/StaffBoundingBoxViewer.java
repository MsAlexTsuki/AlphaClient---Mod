package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays dimensions and bounds of the entity under the crosshair. */
public final class StaffBoundingBoxViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-bounding-box-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo||mc.objectMouseOver==null||mc.objectMouseOver.entityHit==null)return;
  Entity t=mc.objectMouseOver.entityHit;AxisAlignedBB b=t.getEntityBoundingBox();
  mc.fontRendererObj.drawStringWithShadow("Bounding box: "+t.getName(),6,6,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Size W/H/D: "+String.format("%.3f / %.3f / %.3f",b.maxX-b.minX,b.maxY-b.minY,b.maxZ-b.minZ),6,18,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Min: "+String.format("%.2f, %.2f, %.2f",b.minX,b.minY,b.minZ),6,30,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Max: "+String.format("%.2f, %.2f, %.2f",b.maxX,b.maxY,b.maxZ),6,42,0xFFFFFFFF);
 }
}