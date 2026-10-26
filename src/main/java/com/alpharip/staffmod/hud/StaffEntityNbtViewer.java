package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays serialized NBT for the entity under the crosshair. */
public final class StaffEntityNbtViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-entity-nbt-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo||mc.objectMouseOver==null||mc.objectMouseOver.entityHit==null)return;
  Entity target=mc.objectMouseOver.entityHit;NBTTagCompound nbt=new NBTTagCompound();target.writeToNBT(nbt);String s=nbt.toString();if(s.length()>170)s=s.substring(0,167)+"...";
  mc.fontRendererObj.drawStringWithShadow("Entity NBT: "+s,6,6,0xFFFFFFFF);
 }
}