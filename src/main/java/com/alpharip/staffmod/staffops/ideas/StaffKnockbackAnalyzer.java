package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Estimates observed displacement after damage; this is not a server-authoritative knockback measurement. */
public final class StaffKnockbackAnalyzer {
 private double startX,startY,startZ,dx,dy,dz;private long started;private String target="No recent damage";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-knockback-analyzer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onHurt(LivingHurtEvent e){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||!(e.entityLiving instanceof EntityLivingBase))return;EntityLivingBase victim=(EntityLivingBase)e.entityLiving;if(victim==mc.thePlayer||victim.getLastAttacker()!=mc.thePlayer)return;startX=victim.posX;startY=victim.posY;startZ=victim.posZ;target=victim.getName();started=System.currentTimeMillis();dx=dy=dz=0;}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;if(started>0){net.minecraft.entity.Entity found=null;for(Object o:mc.theWorld.loadedEntityList){if(o instanceof EntityLivingBase&&target.equals(((EntityLivingBase)o).getName())){found=(net.minecraft.entity.Entity)o;break;}}if(found!=null){dx=found.posX-startX;dy=found.posY-startY;dz=found.posZ-startZ;}}mc.fontRendererObj.drawStringWithShadow("Knockback estimate: "+target,6,6,-1);mc.fontRendererObj.drawStringWithShadow(String.format("Delta X %.2f Y %.2f Z %.2f | %.1fs",dx,dy,dz,started==0?0:(System.currentTimeMillis()-started)/1000.0),6,18,0xFFCCCCCC);}
}