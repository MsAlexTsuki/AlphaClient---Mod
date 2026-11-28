package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayDeque;import java.util.Deque;
/** Keeps a short local record of commands typed by the player; it does not observe other players' commands. */
public final class StaffCommandTracker {
 private final Deque<String> commands=new ArrayDeque<String>();
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-command-tracker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(!enabled()||e.message==null||!e.message.startsWith("/"))return;commands.addFirst(e.message);while(commands.size()>5)commands.removeLast();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;int y=6;mc.fontRendererObj.drawStringWithShadow("Recent Commands",6,y,0xFFFFCC55);for(String s:commands){y+=12;mc.fontRendererObj.drawStringWithShadow(s,6,y,0xFFFFFFFF);}}
}