package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
public final class HeldItemDurabilityHud {
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text event) {
  Minecraft mc=Minecraft.getMinecraft();
  if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  AlphaModule m=AlphaModuleRegistry.find("durability-hud");
  if(m==null||!m.isEnabled()||!m.isUnlocked())return;
  ItemStack s=mc.thePlayer.getHeldItem();
  if(s==null||!s.isItemStackDamageable())return;
  int left=Math.max(0,s.getMaxDamage()-s.getItemDamage());
  mc.fontRendererObj.drawStringWithShadow("Held item: "+left+"/"+s.getMaxDamage(),6,222,0xFFFFFFFF);
 }
}
