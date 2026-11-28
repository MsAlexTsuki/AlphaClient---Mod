package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.security.MessageDigest;
/** Computes a SHA-256 digest for a locally entered evidence note; does not hash files. */
public final class EvidenceHashTracker {
 private String note="",hash="No note hashed";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-hash-tracker");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message==null||!e.message.startsWith("/evidencehash "))return;note=e.message.substring(14);try{byte[] b=MessageDigest.getInstance("SHA-256").digest(note.getBytes("UTF-8"));StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format(java.util.Locale.ROOT,"%02x",v&255));hash=s.toString();}catch(Exception ex){hash="Hash unavailable";}e.setCanceled(true);}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence SHA-256 (note): "+hash,6,122,0xFFB8D7FF);}
}