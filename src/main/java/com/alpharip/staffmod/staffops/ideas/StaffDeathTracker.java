package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayDeque;
import java.util.Deque;
/** Records player deaths observed by this client; it is not a server-wide log. */
public final class StaffDeathTracker {
 private final Deque<String> deaths=new ArrayDeque<String>();
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-death-tracker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onDeath(LivingDeathEvent e){if(!enabled()||!(e.entityLiving instanceof EntityPlayer))return;EntityPlayer p=(EntityPlayer)e.entityLiving;Minecraft mc=Minecraft.getMinecraft();if(mc.theWorld==null)return;deaths.addFirst(p.getName()+" died");while(deaths.size()>5)deaths.removeLast();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;int y=6;mc.fontRendererObj.drawStringWithShadow("Death Tracker",6,y,0xFFFF5555);for(String s:deaths){y+=12;mc.fontRendererObj.drawStringWithShadow(s,6,y,0xFFFFFFFF);}}
}