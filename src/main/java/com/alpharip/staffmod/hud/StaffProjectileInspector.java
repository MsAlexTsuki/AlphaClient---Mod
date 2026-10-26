package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays motion details for a targeted projectile. */
public final class StaffProjectileInspector {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-projectile-inspector");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return; Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo||mc.objectMouseOver==null||mc.objectMouseOver.typeOfHit!=MovingObjectPosition.MovingObjectType.ENTITY)return;
  Entity x=mc.objectMouseOver.entityHit;if(!(x instanceof EntityArrow)&&!(x instanceof EntityThrowable)&&!(x instanceof EntityFireball))return;
  double s=Math.sqrt(x.motionX*x.motionX+x.motionY*x.motionY+x.motionZ*x.motionZ);
  mc.fontRendererObj.drawStringWithShadow("Projectile: "+x.getClass().getSimpleName(),6,6,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow(String.format("Entity ID %d | Speed %.3f blocks/tick",x.getEntityId(),s),6,18,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow(String.format("Motion %.3f, %.3f, %.3f",x.motionX,x.motionY,x.motionZ),6,30,0xFFFFFFFF);
 }
}