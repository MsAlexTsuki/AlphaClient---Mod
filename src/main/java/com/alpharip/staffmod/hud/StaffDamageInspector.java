package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;import com.alpharip.staffmod.modules.AlphaModuleRegistry;import net.minecraft.client.Minecraft;import net.minecraftforge.client.event.RenderGameOverlayEvent;import net.minecraftforge.event.entity.living.LivingHurtEvent;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Records the most recent living-entity damage event observed in the client world. */
public final class StaffDamageInspector{
 private String target="none";private float amount;private long at;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-damage-inspector");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onHurt(LivingHurtEvent e){Minecraft mc=Minecraft.getMinecraft();if(!enabled()||mc==null||mc.theWorld==null||e.entityLiving==null||e.entityLiving.worldObj!=mc.theWorld)return;target=e.entityLiving.getName();amount=e.ammount;at=System.currentTimeMillis();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;String age=at==0L?"no event":((System.currentTimeMillis()-at)/1000L)+"s ago";mc.fontRendererObj.drawStringWithShadow("Damage Inspector (client-observed)",6,6,0xFFFFFFFF);mc.fontRendererObj.drawStringWithShadow("Last target: "+target+" | amount: "+String.format("%.2f",amount)+" | "+age,6,18,0xFFFFFFFF);}
}