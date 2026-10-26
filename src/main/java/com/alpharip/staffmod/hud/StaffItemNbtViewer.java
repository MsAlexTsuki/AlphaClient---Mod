package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays the held item's serialized NBT, shortened for HUD readability. */
public final class StaffItemNbtViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-item-nbt-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  ItemStack stack=mc.thePlayer.getHeldItem();if(stack==null){mc.fontRendererObj.drawStringWithShadow("Held item NBT: no item held",6,6,0xFFFFFFFF);return;}
  NBTTagCompound nbt=new NBTTagCompound();stack.writeToNBT(nbt);String s=nbt.toString();if(s.length()>170)s=s.substring(0,167)+"...";
  mc.fontRendererObj.drawStringWithShadow("Item NBT: "+s,6,6,0xFFFFFFFF);
 }
}