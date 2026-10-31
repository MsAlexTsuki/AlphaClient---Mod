package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayDeque;
import java.util.Deque;
/** Records a short client-observed coordinate trail for the player currently targeted. */
public final class StaffPlayerTrail {
 private final Deque<String> trail=new ArrayDeque<String>();private String tracked="";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-player-trail");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;Entity target=mc.objectMouseOver==null?null:mc.objectMouseOver.entityHit;if(target instanceof EntityPlayer&&target!=mc.thePlayer){String name=target.getName();if(!name.equals(tracked)){tracked=name;trail.clear();}String point=String.format("%.1f %.1f %.1f",target.posX,target.posY,target.posZ);if(trail.isEmpty()||!trail.peekLast().equals(point))trail.addLast(point);while(trail.size()>6)trail.removeFirst();}mc.fontRendererObj.drawStringWithShadow("Player Trail: "+(tracked.isEmpty()?"target a player":tracked),6,6,0xFFFFFF55);int y=18;for(String p:trail){mc.fontRendererObj.drawStringWithShadow(p,6,y,0xFFCCCCCC);y+=10;}}
}