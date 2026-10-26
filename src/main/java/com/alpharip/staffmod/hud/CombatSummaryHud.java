package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Summarizes locally observed damage events after five seconds without a tracked hit. */
public final class CombatSummaryHud {
 private int hits;private float dealt,taken;private long lastHit,summaryUntil;private String summary="No combat recorded";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("combat-summary-hud");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onHurt(LivingHurtEvent e){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||!(e.entityLiving instanceof EntityLivingBase))return;EntityLivingBase victim=e.entityLiving;if(victim==mc.thePlayer){taken+=e.ammount;lastHit=System.currentTimeMillis();return;}if(victim.getLastAttacker()==mc.thePlayer){hits++;dealt+=e.ammount;lastHit=System.currentTimeMillis();}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;long now=System.currentTimeMillis();if(lastHit>0&&now-lastHit>5000&&summaryUntil<now){summary=String.format("Combat summary: %d hits | %.1f dealt | %.1f taken",hits,dealt,taken);summaryUntil=now+8000;hits=0;dealt=0;taken=0;lastHit=0;}if(summaryUntil>now){mc.fontRendererObj.drawStringWithShadow(summary,6,6,0xFFFFFF55);}else if(lastHit>0){mc.fontRendererObj.drawStringWithShadow(String.format("Combat: %d hits | %.1f dealt | %.1f taken",hits,dealt,taken),6,6,0xFFFFFFFF);}}
}