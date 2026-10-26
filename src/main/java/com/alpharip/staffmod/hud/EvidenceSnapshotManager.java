package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;
/** Maintains a small in-memory list of manually named evidence snapshots. */
public final class EvidenceSnapshotManager {
 private final List<String> snapshots=new ArrayList<String>();
 private String latest="No snapshots";
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("evidence-snapshot-manager");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void onChat(ClientChatEvent e){if(e.message==null)return;if(e.message.startsWith("/evidencesnapshot add ")){String n=e.message.substring(21).trim();if(!n.isEmpty()&&snapshots.size()<10){snapshots.add(n.length()>36?n.substring(0,36):n);latest="Saved: "+snapshots.get(snapshots.size()-1);}else latest="Name required or snapshot limit reached";e.setCanceled(true);}else if(e.message.equalsIgnoreCase("/evidencesnapshot clear")){snapshots.clear();latest="Snapshots cleared";e.setCanceled(true);}}
 @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;mc.fontRendererObj.drawStringWithShadow("Evidence Snapshots: "+snapshots.size()+" — "+latest,6,232,0xFFFFFFFF);}
}