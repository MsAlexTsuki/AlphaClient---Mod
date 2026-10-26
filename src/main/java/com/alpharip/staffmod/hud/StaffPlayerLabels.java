package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
import net.minecraft.entity.player.EntityPlayer;
public final class StaffPlayerLabels {
private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-player-labels");return m!=null&&m.isEnabled()&&m.isUnlocked();}
@SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.theWorld==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;int y=6,n=0;for(Object o:m.theWorld.playerEntities){if(!(o instanceof EntityPlayer)||o==m.thePlayer)continue;EntityPlayer p=(EntityPlayer)o;double d=m.thePlayer.getDistanceToEntity(p);if(d>24)continue;m.fontRendererObj.drawStringWithShadow(p.getName()+String.format(" [%.0fm]",d),6,y,0xFFFFFF55);y+=10;if(++n>=8)break;}}
}