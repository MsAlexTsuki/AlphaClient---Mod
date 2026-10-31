package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays the current client raycast distance to a targeted block. */
public final class StaffBlockReachViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-block-reach-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.objectMouseOver==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;if(mc.objectMouseOver.typeOfHit!=MovingObjectPosition.MovingObjectType.BLOCK){mc.fontRendererObj.drawStringWithShadow("Block Reach: no block targeted",6,6,0xFFCCCCCC);return;}double d=mc.thePlayer.getDistance(mc.objectMouseOver.getBlockPos().getX()+0.5,mc.objectMouseOver.getBlockPos().getY()+0.5,mc.objectMouseOver.getBlockPos().getZ()+0.5);mc.fontRendererObj.drawStringWithShadow(String.format("Block Reach: %.2f blocks",d),6,6,0xFFFFFFFF);}
}