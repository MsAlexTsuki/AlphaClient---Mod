package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Tracks local jump count and peak upward motion for basic movement review. */
public final class StaffJumpAnalyzer {
 private int jumps;private boolean wasGrounded=true;private double peak;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-jump-analyzer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;boolean grounded=mc.thePlayer.onGround;if(wasGrounded&&!grounded&&mc.thePlayer.motionY>0){jumps++;peak=Math.max(peak,mc.thePlayer.motionY);}wasGrounded=grounded;mc.fontRendererObj.drawStringWithShadow("Jump Analyzer: "+jumps,6,30,0xFFB8D7FF);mc.fontRendererObj.drawStringWithShadow(String.format(java.util.Locale.ROOT,"Peak upward motion: %.3f",peak),6,41,0xFFB8D7FF);}
}