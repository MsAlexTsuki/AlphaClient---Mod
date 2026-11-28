package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;import com.alpharip.staffmod.modules.AlphaModuleRegistry;import net.minecraft.client.Minecraft;import net.minecraftforge.client.event.MouseEvent;import net.minecraftforge.client.event.RenderGameOverlayEvent;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Counts mouse-button press events observed by Forge. */
public final class StaffClickInspector{
 private long left,right,middle,at;private int last=-1;
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-click-inspector");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onMouse(MouseEvent e){if(!enabled()||!e.buttonstate)return;if(e.button==0)left++;else if(e.button==1)right++;else if(e.button==2)middle++;else return;last=e.button;at=System.currentTimeMillis();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;String l=last<0?"none":last+" ("+((System.currentTimeMillis()-at)/1000L)+"s ago)";mc.fontRendererObj.drawStringWithShadow("Click Inspector",6,6,0xFFFFFFFF);mc.fontRendererObj.drawStringWithShadow("Left "+left+" | Right "+right+" | Middle "+middle,6,18,0xFFFFFFFF);mc.fontRendererObj.drawStringWithShadow("Last button: "+l,6,30,0xFFFFFFFF);}
}