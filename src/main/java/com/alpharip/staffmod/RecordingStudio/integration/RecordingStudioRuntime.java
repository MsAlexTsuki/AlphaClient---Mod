package com.alpharip.staffmod.RecordingStudio.integration;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;import com.alpharip.staffmod.RecordingStudio.ui.RecordingStudioScreen;import net.minecraft.client.Minecraft;import net.minecraftforge.common.MinecraftForge;import net.minecraftforge.event.world.WorldEvent;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.common.gameevent.TickEvent;
/** Independent Forge lifecycle; it registers no Build Timelapse event handlers. */
public final class RecordingStudioRuntime {
 private final RecordingStudioController controller;private boolean started,closed;
 public RecordingStudioRuntime(RecordingStudioController c){if(c==null)throw new IllegalArgumentException("controller");controller=c;}
 public synchronized void open(Minecraft mc){if(mc==null||mc.theWorld==null||mc.thePlayer==null)throw new IllegalStateException("Recording Studio can only open inside a loaded world");if(closed)throw new IllegalStateException("Recording Studio runtime is shut down");if(!started){MinecraftForge.EVENT_BUS.register(this);started=true;}mc.displayGuiScreen(new RecordingStudioScreen(controller));}
 @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;Minecraft mc=Minecraft.getMinecraft();if(mc==null||mc.theWorld==null||mc.thePlayer==null)shutdown();else controller.tick(System.currentTimeMillis());}
 @SubscribeEvent public void onWorldUnload(WorldEvent.Unload e){if(e.world!=null&&e.world.isRemote)shutdown();}
 public synchronized void shutdown(){if(closed)return;closed=true;if(started)MinecraftForge.EVENT_BUS.unregister(this);started=false;controller.shutdown();}
}