package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Inspects tile entities attached to the selected block. */
public final class StaffTileEntityInspector {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-tile-entity-inspector");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo||mc.objectMouseOver==null||mc.objectMouseOver.getBlockPos()==null)return;
  BlockPos p=mc.objectMouseOver.getBlockPos();TileEntity t=mc.theWorld.getTileEntity(p);
  if(t==null){mc.fontRendererObj.drawStringWithShadow("Tile entity: none at "+p.getX()+", "+p.getY()+", "+p.getZ(),6,6,0xFFFFFFFF);return;}
  mc.fontRendererObj.drawStringWithShadow("Tile entity: "+t.getClass().getName(),6,6,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Pos: "+p.getX()+", "+p.getY()+", "+p.getZ()+" | invalid "+t.isInvalid(),6,18,0xFFFFFFFF);
 }
}