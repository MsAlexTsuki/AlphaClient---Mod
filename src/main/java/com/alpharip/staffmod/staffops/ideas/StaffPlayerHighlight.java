package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
/** Highlights the player currently selected by /alphahighlight <name>. */
public final class StaffPlayerHighlight {
 private String target="";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-player-highlight");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(net.minecraftforge.client.event.ClientChatEvent e){if(e.message!=null&&e.message.startsWith("/alphahighlight ")){target=e.message.substring(16).trim();e.setCanceled(true);}}
 @SubscribeEvent public void onWorld(RenderWorldLastEvent e){Minecraft mc=Minecraft.getMinecraft();if(!enabled()||mc.theWorld==null||mc.thePlayer==null||target.isEmpty())return;for(Object o:mc.theWorld.playerEntities){EntityPlayer p=(EntityPlayer)o;if(p.getName().equalsIgnoreCase(target)){double x=p.lastTickPosX+(p.posX-p.lastTickPosX)*e.partialTicks-mc.getRenderManager().viewerPosX;double y=p.lastTickPosY+(p.posY-p.lastTickPosY)*e.partialTicks-mc.getRenderManager().viewerPosY;double z=p.lastTickPosZ+(p.posZ-p.lastTickPosZ)*e.partialTicks-mc.getRenderManager().viewerPosZ;GL11.glPushMatrix();GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glLineWidth(2);GL11.glColor4f(1f,.8f,.1f,1f);net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox(p.getEntityBoundingBox().offset(-mc.getRenderManager().viewerPosX,-mc.getRenderManager().viewerPosY,-mc.getRenderManager().viewerPosZ));GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glPopMatrix();break;}}}
}