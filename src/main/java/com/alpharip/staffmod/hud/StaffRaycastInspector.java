package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Reports what the client's current crosshair raycast intersects. */
public final class StaffRaycastInspector {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-raycast-inspector");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  MovingObjectPosition h=mc.objectMouseOver;if(h==null||h.typeOfHit==MovingObjectPosition.MovingObjectType.MISS){mc.fontRendererObj.drawStringWithShadow("Raycast: MISS",6,6,0xFFFFFFFF);return;}
  double d=mc.thePlayer.getPositionEyes(1.0F).distanceTo(h.hitVec);String detail;
  if(h.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK&&h.getBlockPos()!=null){BlockPos p=h.getBlockPos();detail="block "+p.getX()+", "+p.getY()+", "+p.getZ()+" face "+h.sideHit;}
  else if(h.entityHit!=null)detail="entity "+h.entityHit.getName()+" ID "+h.entityHit.getEntityId();else detail=h.typeOfHit.toString();
  mc.fontRendererObj.drawStringWithShadow("Raycast: "+detail+" | "+String.format("%.2f",d)+" blocks",6,6,0xFFFFFFFF);
 }
}