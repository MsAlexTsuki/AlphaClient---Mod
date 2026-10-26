package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Counts locally observed hits against entities last attacked by the local player. */
public final class HitsPerFightHud {
 private int hits;private long lastHit;private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("hits-per-fight-hud");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onHurt(LivingHurtEvent e){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||!(e.entityLiving instanceof EntityLivingBase)||e.entityLiving==mc.thePlayer)return;EntityLivingBase target=(EntityLivingBase)e.entityLiving;if(target.getLastAttacker()==mc.thePlayer){hits++;lastHit=System.currentTimeMillis();}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;long now=System.currentTimeMillis();if(lastHit>0&&now-lastHit>10000){hits=0;lastHit=0;}mc.fontRendererObj.drawStringWithShadow("Hits this fight: "+hits,6,6,0xFFFFFFFF);}
 public void reset(){hits=0;lastHit=0;}
}