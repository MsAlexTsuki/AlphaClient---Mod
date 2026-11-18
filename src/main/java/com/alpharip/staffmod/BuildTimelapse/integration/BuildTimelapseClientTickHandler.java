package com.alpharip.staffmod.BuildTimelapse.integration;
import com.alpharip.staffmod.BuildTimelapse.core.BuildTimelapseController;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.common.gameevent.TickEvent;
/** Register on the Forge event bus on the Minecraft client. */
public final class BuildTimelapseClientTickHandler {
 private final BuildTimelapseController controller;public BuildTimelapseClientTickHandler(BuildTimelapseController c){if(c==null)throw new IllegalArgumentException();controller=c;}
 @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event){if(event.phase==TickEvent.Phase.END)controller.tick(System.currentTimeMillis());}
}
