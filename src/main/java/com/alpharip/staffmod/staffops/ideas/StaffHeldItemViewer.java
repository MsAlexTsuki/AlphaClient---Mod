package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffHeldItemViewer {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-held-item-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;net.minecraft.item.ItemStack s=m.thePlayer.getHeldItem();m.fontRendererObj.drawStringWithShadow(s==null?"Held Item: empty":"Held Item: "+s.getDisplayName()+" x"+s.stackSize,6,6,-1);}
}