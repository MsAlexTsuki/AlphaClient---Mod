package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First-pass utility HUDs and warnings for movement, inventory, armor, and targets. */
public final class UtilityTrackingHudModules {
 private boolean on(String id){AlphaModule m=AlphaModuleRegistry.find(id);return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event){
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  if(on("movement-state-hud"))mc.fontRendererObj.drawStringWithShadow("Movement: "+(mc.gameSettings.keyBindForward.isKeyDown()?"W":"-")+" "+(mc.gameSettings.keyBindBack.isKeyDown()?"S":"-")+" "+(mc.gameSettings.keyBindLeft.isKeyDown()?"A":"-")+" "+(mc.gameSettings.keyBindRight.isKeyDown()?"D":"-")+" | "+(mc.thePlayer.onGround?"GROUND":"AIR"),6,490,0xFFFFFFFF);
  if(on("auto-walk-indicator"))mc.fontRendererObj.drawStringWithShadow("Forward: "+(mc.gameSettings.keyBindForward.isKeyDown()?"HELD":"RELEASED"),6,502,0xFFFFFFFF);
  if(on("jump-reset-indicator"))mc.fontRendererObj.drawStringWithShadow("Jump: "+(mc.gameSettings.keyBindJump.isKeyDown()?"HELD":"RELEASED")+" | "+(mc.thePlayer.onGround?"GROUNDED":"AIRBORNE"),6,514,0xFFFFFFFF);
  if(on("fall-distance-warning")&&mc.thePlayer.fallDistance>=3.0F)mc.fontRendererObj.drawStringWithShadow("FALL WARNING: "+String.format("%.1f",mc.thePlayer.fallDistance)+" blocks",6,526,0xFFFF5555);
  if(on("void-warning")&&mc.thePlayer.posY<8.0D&&mc.thePlayer.posY>0.0D){boolean solid=false;for(int y=1;y<=6;y++)if(!mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX,mc.thePlayer.posY-y,mc.thePlayer.posZ))){solid=true;break;}if(!solid)mc.fontRendererObj.drawStringWithShadow("VOID WARNING",6,538,0xFFFF3333);}
  if(on("low-durability-armor")){int low=0;for(int slot=1;slot<=4;slot++){ItemStack it=mc.thePlayer.getEquipmentInSlot(slot);if(it!=null&&it.isItemStackDamageable()&&it.getMaxDamage()-it.getItemDamage()<=Math.max(10,it.getMaxDamage()/10))low++;}if(low>0)mc.fontRendererObj.drawStringWithShadow("Armor warning: "+low+" piece(s) nearly broken",6,550,0xFFFFAA33);}
  if(on("potion-effect-count"))mc.fontRendererObj.drawStringWithShadow("Active effects: "+mc.thePlayer.getActivePotionEffects().size(),6,562,0xFFFFFFFF);
  if(on("inventory-full-warning")){boolean empty=false;for(ItemStack it:mc.thePlayer.inventory.mainInventory)if(it==null){empty=true;break;}if(!empty)mc.fontRendererObj.drawStringWithShadow("INVENTORY FULL",6,574,0xFFFFAA33);}
  if(on("low-food-sprint-warning")&&mc.thePlayer.getFoodStats().getFoodLevel()<=6)mc.fontRendererObj.drawStringWithShadow("LOW FOOD: sprinting may be unavailable",6,586,0xFFFFAA33);
  if(on("target-type-hud")){Entity e=mc.objectMouseOver==null?null:mc.objectMouseOver.entityHit;if(e!=null){String type=e.getClass().getSimpleName();if(e instanceof EntityLivingBase){EntityLivingBase l=(EntityLivingBase)e;type+=" | HP "+String.format("%.1f",l.getHealth())+"/"+String.format("%.1f",l.getMaxHealth());}mc.fontRendererObj.drawStringWithShadow("Target: "+type,6,598,0xFFFFFFFF);}}
 }
}