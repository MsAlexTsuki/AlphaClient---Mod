package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;import com.alpharip.staffmod.modules.AlphaModuleRegistry;import net.minecraft.client.Minecraft;import net.minecraftforge.client.event.RenderGameOverlayEvent;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.common.gameevent.TickEvent;
/** Measures local client tick cadence, not remote server TPS. */
public final class StaffTpsMonitor{
 private long start;private int ticks;private double rate;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-tps-monitor");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onTick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||!enabled())return;long now=System.currentTimeMillis();if(start==0L)start=now;ticks++;long elapsed=now-start;if(elapsed>=1000L){rate=ticks*1000.0D/elapsed;ticks=0;start=now;}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Client Tick Rate (not server TPS)",6,6,0xFFFFFFFF);mc.fontRendererObj.drawStringWithShadow(String.format("%.1f ticks/s",rate),6,18,0xFFFFFFFF);}
}