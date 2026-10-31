package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays tile-entity NBT for the block under the crosshair. */
public final class StaffBlockNbtViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-block-nbt-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo||mc.objectMouseOver==null||mc.objectMouseOver.getBlockPos()==null)return;
  BlockPos p=mc.objectMouseOver.getBlockPos();TileEntity t=mc.theWorld.getTileEntity(p);
  if(t==null){mc.fontRendererObj.drawStringWithShadow("Block NBT: no tile entity",6,6,0xFFFFFFFF);return;}
  NBTTagCompound nbt=new NBTTagCompound();t.writeToNBT(nbt);String s=nbt.toString();if(s.length()>170)s=s.substring(0,167)+"...";
  mc.fontRendererObj.drawStringWithShadow("Block NBT: "+s,6,6,0xFFFFFFFF);
 }
}