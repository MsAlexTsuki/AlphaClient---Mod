package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;import com.alpharip.staffmod.modules.AlphaModuleRegistry;import net.minecraft.client.Minecraft;import net.minecraftforge.client.event.RenderGameOverlayEvent;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.common.network.FMLNetworkEvent;
/** Monitors Forge custom-packet events; vanilla protocol packets are not counted. */
public final class StaffPacketMonitor{
 private long inbound;private boolean connected;private String server="not connected";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-packet-monitor");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onCustomPacket(FMLNetworkEvent.ClientCustomPacketEvent e){if(enabled())inbound++;}
 @SubscribeEvent public void onConnected(FMLNetworkEvent.ClientConnectedToServerEvent e){connected=true;Minecraft mc=Minecraft.getMinecraft();server=mc!=null&&mc.getCurrentServerData()!=null?mc.getCurrentServerData().serverIP:"connection established";}
 @SubscribeEvent public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e){connected=false;server="not connected";}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.thePlayer==null||mc.fontRendererObj==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Packet Monitor (Forge custom packets)",6,6,0xFFFFFFFF);mc.fontRendererObj.drawStringWithShadow("Inbound custom packets: "+inbound,6,18,0xFFFFFFFF);mc.fontRendererObj.drawStringWithShadow("Connection: "+(connected?server:"not connected"),6,30,0xFFFFFFFF);}
}