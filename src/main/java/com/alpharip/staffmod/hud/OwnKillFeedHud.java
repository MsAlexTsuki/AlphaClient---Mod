package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayDeque;
import java.util.Deque;
/** Client-observed kill feed; only credits deaths where the local player is the last attacker. */
public final class OwnKillFeedHud {
 private final Deque<String> entries=new ArrayDeque<String>();
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("own-kill-feed-hud");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onDeath(LivingDeathEvent e){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||!(e.entityLiving instanceof EntityLivingBase))return;EntityLivingBase victim=e.entityLiving;if(victim==mc.thePlayer||victim.getLastAttacker()!=mc.thePlayer)return;String name=victim instanceof EntityPlayer?((EntityPlayer)victim).getName():victim.getName();entries.addFirst(name+" eliminated");while(entries.size()>5)entries.removeLast();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Your Kill Feed",6,6,0xFFFFD45A);int y=18;for(String s:entries){mc.fontRendererObj.drawStringWithShadow(s,6,y,0xFFFFFFFF);y+=10;}}
}