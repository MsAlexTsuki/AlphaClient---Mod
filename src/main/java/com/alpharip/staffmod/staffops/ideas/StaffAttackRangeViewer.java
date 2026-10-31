package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Displays the client-side distance to the entity currently targeted by the crosshair. */
public final class StaffAttackRangeViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-attack-range-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.objectMouseOver==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;Entity t=mc.objectMouseOver.entityHit;if(t==null){mc.fontRendererObj.drawStringWithShadow("Attack Range: no entity targeted",6,6,0xFFCCCCCC);return;}mc.fontRendererObj.drawStringWithShadow(String.format("Attack Range: %.2f blocks | %s",mc.thePlayer.getDistanceToEntity(t),t.getName()),6,6,0xFFFFFFFF);}
}