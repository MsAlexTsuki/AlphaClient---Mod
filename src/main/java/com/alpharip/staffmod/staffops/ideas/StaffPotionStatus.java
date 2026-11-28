package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffPotionStatus {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-potion-status");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;int y=6,n=0;m.fontRendererObj.drawStringWithShadow("Active Potion Effects",6,y,-1);y+=12;for(Object o:m.thePlayer.getActivePotionEffects()){net.minecraft.potion.PotionEffect p=(net.minecraft.potion.PotionEffect)o;net.minecraft.potion.Potion potion=net.minecraft.potion.Potion.potionTypes[p.getPotionID()];String name=potion==null?("Effect "+p.getPotionID()):potion.getName();m.fontRendererObj.drawStringWithShadow(name+" "+(p.getAmplifier()+1)+" | "+(p.getDuration()/20)+"s",6,y,0xFFCCCCCC);y+=10;if(++n>=6)break;}}
}