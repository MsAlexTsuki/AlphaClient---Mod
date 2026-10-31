package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First-pass implementations for ten independent combat and PvP information modules. */
public final class CombatTrackingHudModules {
 private long lastDamageAt, pearlUntil, lastHitAt; private int previousHurt, previousTarget=-1, combo; private boolean wasUsing;
 private boolean on(Minecraft mc,String id){AlphaModule m=AlphaModuleRegistry.find(id);return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event){
  Minecraft mc=Minecraft.getMinecraft(); if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  Entity target=mc.objectMouseOver==null?null:mc.objectMouseOver.entityHit;
  if(on(mc,"target-armor-hud")&&target instanceof EntityLivingBase){StringBuilder s=new StringBuilder("Target armor: ");for(int i=4;i>=1;i--){ItemStack it=target.getEquipmentInSlot(i);if(i!=4)s.append(" | ");s.append(it==null?"-":(it.isItemStackDamageable()?(it.getMaxDamage()-it.getItemDamage())+"/"+it.getMaxDamage():it.getDisplayName()));}mc.fontRendererObj.drawStringWithShadow(s.toString(),6,382,0xFFFFFFFF);}
  if(on(mc,"target-equipment-hud")&&target instanceof EntityLivingBase){ItemStack held=((EntityLivingBase)target).getHeldItem();mc.fontRendererObj.drawStringWithShadow("Target item: "+(held==null?"Empty":held.getDisplayName()),6,394,0xFFFFFFFF);}
  if(on(mc,"arrow-tracker")){int count=0;double near=9999;for(Object o:mc.theWorld.loadedEntityList)if(o instanceof EntityArrow){double d=mc.thePlayer.getDistanceToEntity((EntityArrow)o);if(d<=32){count++;near=Math.min(near,d);}}mc.fontRendererObj.drawStringWithShadow(count==0?"Arrows nearby: 0":"Arrows nearby: "+count+" | nearest "+String.format("%.1f",near)+"m",6,406,0xFFFFFFFF);}
  if(on(mc,"pearl-tracker")){int count=0;double near=9999;for(Object o:mc.theWorld.loadedEntityList)if(o instanceof EntityEnderPearl){double d=mc.thePlayer.getDistanceToEntity((EntityEnderPearl)o);if(d<=48){count++;near=Math.min(near,d);}}mc.fontRendererObj.drawStringWithShadow(count==0?"Pearls nearby: 0":"Pearls nearby: "+count+" | nearest "+String.format("%.1f",near)+"m",6,418,0xFFFFFFFF);}
  if(on(mc,"projectile-warning")){double near=9999;String kind="";for(Object o:mc.theWorld.loadedEntityList){if(!(o instanceof EntityArrow)&&!(o instanceof EntityEnderPearl))continue;double d=mc.thePlayer.getDistanceToEntity((Entity)o);if(d<near&&d<=6){near=d;kind=o instanceof EntityArrow?"Arrow":"Pearl";}}if(!kind.isEmpty())mc.fontRendererObj.drawStringWithShadow("WARNING: "+kind+" nearby ("+String.format("%.1f",near)+"m)",6,430,0xFFFF5555);}
  int hurt=mc.thePlayer.hurtTime;if(on(mc,"combat-timer")){if(hurt>previousHurt)lastDamageAt=System.currentTimeMillis();String t=lastDamageAt==0?"Combat timer: --":"Last damage: "+String.format("%.1f",(System.currentTimeMillis()-lastDamageAt)/1000.0)+"s";mc.fontRendererObj.drawStringWithShadow(t,6,442,0xFFFFFFFF);}
  if(on(mc,"combo-counter")){if(!(target instanceof EntityLivingBase)){if(System.currentTimeMillis()-lastHitAt>1500)combo=0;previousHurt=0;}else{EntityLivingBase living=(EntityLivingBase)target;int id=living.getEntityId(),h=living.hurtTime;if(id!=previousTarget){previousTarget=id;previousHurt=0;combo=0;}if(h>previousHurt){combo++;lastHitAt=System.currentTimeMillis();}if(System.currentTimeMillis()-lastHitAt>1500)combo=0;previousHurt=h;}mc.fontRendererObj.drawStringWithShadow("Combo: "+combo,6,454,0xFFFFFFFF);}
  if(on(mc,"sprint-reset-indicator"))mc.fontRendererObj.drawStringWithShadow("Sprint: "+(mc.thePlayer.isSprinting()?"SPRINTING":"NOT SPRINTING")+" | "+(mc.gameSettings.keyBindForward.isKeyDown()?"W HELD":"W RELEASED"),6,466,0xFFFFFFFF);
  if(on(mc,"pearl-cooldown")){ItemStack held=mc.thePlayer.getHeldItem();boolean use=mc.gameSettings.keyBindUseItem.isKeyDown();if(use&&!wasUsing&&held!=null&&held.getItem() instanceof ItemEnderPearl)pearlUntil=System.currentTimeMillis()+1000L;long left=pearlUntil-System.currentTimeMillis();mc.fontRendererObj.drawStringWithShadow(left>0?"Pearl cooldown: "+String.format("%.1f",left/1000.0)+"s":"Pearl cooldown: Ready",6,478,0xFFFFFFFF);wasUsing=use;}
  if(on(mc,"hit-indicator")&&target instanceof EntityLivingBase&&((EntityLivingBase)target).hurtTime>0)mc.fontRendererObj.drawStringWithShadow("HIT",mc.displayWidth/4-8,mc.displayHeight/4+24,0xFF55FF55);
  previousHurt=hurt;
 }
}