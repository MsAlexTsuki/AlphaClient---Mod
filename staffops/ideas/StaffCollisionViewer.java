package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Inspects the collision box of the block under the crosshair. */
public final class StaffCollisionViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-collision-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo||mc.objectMouseOver==null||mc.objectMouseOver.getBlockPos()==null)return;
  BlockPos p=mc.objectMouseOver.getBlockPos();IBlockState s=mc.theWorld.getBlockState(p);Block block=s.getBlock();AxisAlignedBB b=block.getCollisionBoundingBox(mc.theWorld,p,s);
  if(b==null){mc.fontRendererObj.drawStringWithShadow("Collision box: none",6,6,0xFFFFFFFF);return;}
  mc.fontRendererObj.drawStringWithShadow("Collision box: "+block.getRegistryName(),6,6,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Size: "+String.format("%.3f x %.3f x %.3f",b.maxX-b.minX,b.maxY-b.minY,b.maxZ-b.minZ),6,18,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Bounds: "+String.format("%.2f, %.2f, %.2f -> %.2f, %.2f, %.2f",b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ),6,30,0xFFFFFFFF);
 }
}