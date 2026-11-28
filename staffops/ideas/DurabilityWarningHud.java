package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** First working pass of durability-warning. */
public final class DurabilityWarning {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("durability-warning");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  net.minecraft.item.ItemStack s=mc.thePlayer.getHeldItem(); if(s!=null&&s.isItemStackDamageable()){int left=s.getMaxDamage()-s.getItemDamage(); if(left<=Math.max(1,s.getMaxDamage()/10)) mc.fontRendererObj.drawStringWithShadow("WARNING: held item durability low ("+left+")",6,354,0xFFFF5555);}
 }
}
