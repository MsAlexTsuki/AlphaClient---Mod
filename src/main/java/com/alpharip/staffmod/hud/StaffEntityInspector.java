package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Shows identity and state details for the entity under the crosshair. */
public final class StaffEntityInspector {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-entity-inspector");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo||mc.objectMouseOver==null||mc.objectMouseOver.entityHit==null)return;
  Entity t=mc.objectMouseOver.entityHit;
  mc.fontRendererObj.drawStringWithShadow("Entity: "+t.getName()+" ("+t.getClass().getSimpleName()+") | ID "+t.getEntityId(),6,6,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("UUID: "+t.getUniqueID(),6,18,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Pos: "+String.format("%.2f, %.2f, %.2f",t.posX,t.posY,t.posZ)+" | onGround "+t.onGround,6,30,0xFFFFFFFF);
  if(t instanceof EntityLivingBase){EntityLivingBase l=(EntityLivingBase)t;mc.fontRendererObj.drawStringWithShadow("Health: "+String.format("%.1f/%.1f",l.getHealth(),l.getMaxHealth())+" | hurtTime "+l.hurtTime,6,42,0xFFFFFFFF);}
 }
}