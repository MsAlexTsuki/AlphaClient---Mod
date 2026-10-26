package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Remembers the most recent client-observed position of the player targeted by the crosshair. */
public final class StaffLastPosition {
 private String name="No player recorded";private double x,y,z;private long seen;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-last-position");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;Entity target=mc.objectMouseOver==null?null:mc.objectMouseOver.entityHit;if(target instanceof EntityPlayer&&target!=mc.thePlayer){name=target.getName();x=target.posX;y=target.posY;z=target.posZ;seen=System.currentTimeMillis();}mc.fontRendererObj.drawStringWithShadow("Last Position: "+name,6,6,0xFFFFFF55);mc.fontRendererObj.drawStringWithShadow(seen==0?"No position captured":String.format("%.3f, %.3f, %.3f | %ds ago",x,y,z,(System.currentTimeMillis()-seen)/1000),6,18,0xFFCCCCCC);}
}