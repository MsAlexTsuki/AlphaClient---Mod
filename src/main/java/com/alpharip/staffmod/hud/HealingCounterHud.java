package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Tracks healing events observed by this client; not authoritative server history. */
public final class HealingCounterHud {
 private float total; private long lastEvent;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("healing-counter-hud");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onHeal(LivingHealEvent e){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer!=null&&e.entityLiving==mc.thePlayer){total+=e.amount;lastEvent=System.currentTimeMillis();}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow(String.format("Healing: %.1f HP",total),6,6,0xFF55FF55);if(lastEvent>0)mc.fontRendererObj.drawStringWithShadow("Last heal: "+((System.currentTimeMillis()-lastEvent)/1000)+"s ago",6,18,0xFFCCCCCC);}
 public void reset(){total=0;lastEvent=0;}
}