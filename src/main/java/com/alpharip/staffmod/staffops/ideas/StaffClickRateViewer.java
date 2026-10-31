package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
/** Client-side mouse press rate; this is a local input estimate, not server-verified CPS. */
public final class StaffClickRateViewer {
 private final Deque<Long> left=new ArrayDeque<Long>(),right=new ArrayDeque<Long>();
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-click-rate-viewer");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onMouse(MouseEvent e){if(!e.buttonstate)return;long now=System.currentTimeMillis();Deque<Long> q=e.button==0?left:e.button==1?right:null;if(q==null)return;q.addLast(now);trim(q,now);}
 private void trim(Deque<Long> q,long now){while(!q.isEmpty()&&now-q.peekFirst()>1000)q.removeFirst();}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;long now=System.currentTimeMillis();trim(left,now);trim(right,now);mc.fontRendererObj.drawStringWithShadow("Click Rate | L "+left.size()+" CPS | R "+right.size()+" CPS",6,6,0xFFFFFFFF);}
}