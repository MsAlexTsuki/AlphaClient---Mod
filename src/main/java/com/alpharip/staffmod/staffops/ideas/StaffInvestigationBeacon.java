package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffInvestigationBeacon {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-investigation-beacon");return m!=null&&m.isEnabled()&&m.isUnlocked();}
private String target="No target";private double x,y,z;
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;if(m.objectMouseOver!=null&&m.objectMouseOver.entityHit!=null){net.minecraft.entity.Entity t=m.objectMouseOver.entityHit;target=t.getName();x=t.posX;y=t.posY;z=t.posZ;}m.fontRendererObj.drawStringWithShadow("Investigation Beacon",6,6,-1);m.fontRendererObj.drawStringWithShadow(target+String.format(" @ %.1f %.1f %.1f",x,y,z),6,18,0xFFFFFF55);}
}