package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Shows the local permission-label status for the staff utility suite. */
public final class StaffPermissionLabels {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-permission-labels");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Permissions: client-side module access",6,30,0xFFB8D7FF);mc.fontRendererObj.drawStringWithShadow("Server permissions are not verified here",6,41,0xFFAAAAAA);}
}