package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffStatusLabels {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-status-labels");return m!=null&&m.isEnabled()&&m.isUnlocked();}
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;String s=m.thePlayer.isDead?"DEAD":m.thePlayer.isBurning()?"BURNING":m.thePlayer.isSneaking()?"SNEAKING":m.thePlayer.isSprinting()?"SPRINTING":m.thePlayer.onGround?"GROUNDED":"AIRBORNE";m.fontRendererObj.drawStringWithShadow("Local Status: "+s,6,6,-1);m.fontRendererObj.drawStringWithShadow(String.format("Health %.1f | Food %d | Dimension %d",m.thePlayer.getHealth(),m.thePlayer.getFoodStats().getFoodLevel(),m.thePlayer.dimension),6,18,0xFFCCCCCC);}
}