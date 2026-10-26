package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Shows details for the chunk containing the local player. */
public final class StaffChunkInspector {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-chunk-inspector");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){
  if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;
  Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;
  BlockPos p=new BlockPos(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ);Chunk c=mc.theWorld.getChunkFromBlockCoords(p);
  int x=p.getX()>>4,z=p.getZ()>>4;
  mc.fontRendererObj.drawStringWithShadow("Chunk: "+x+", "+z+" | loaded "+c.isLoaded(),6,6,0xFFFFFFFF);
  mc.fontRendererObj.drawStringWithShadow("Origin: "+(x<<4)+", "+(z<<4)+" | inhabited "+c.getInhabitedTime()+" ticks",6,18,0xFFFFFFFF);
 }
}