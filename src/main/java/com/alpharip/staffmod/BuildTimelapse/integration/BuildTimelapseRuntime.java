package com.alpharip.staffmod.BuildTimelapse.integration;

import com.alpharip.staffmod.BuildTimelapse.BuildTimelapse;
import com.alpharip.staffmod.BuildTimelapse.ui.BuildTimelapseStudioScreen;
import com.alpharip.staffmod.BuildTimelapse.ui.StudioController;
import com.alpharip.staffmod.BuildTimelapse.ui.StudioScreenModel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Owns Forge event registration for one Build Timelapse session.
 * Call openStudio from the client entry point; closing the root screen shuts it down.
 */
public final class BuildTimelapseRuntime {
    private final BuildTimelapse studio;
    private final BuildTimelapseClientTickHandler tickHandler;
    private final ForgeMinecraftBlockEventBridge blockEvents = new ForgeMinecraftBlockEventBridge();
    private boolean started;
    private boolean closed;

    public BuildTimelapseRuntime(BuildTimelapse studio) {
        if (studio == null) throw new IllegalArgumentException("studio");
        this.studio = studio;
        this.tickHandler = new BuildTimelapseClientTickHandler(studio.getController());
        blockEvents.setListener(new MinecraftBlockEventBridge.Listener() {
            @Override
            public void onBlockChanged(com.alpharip.staffmod.BuildTimelapse.analytics.BlockChangeEvent event) {
                BuildTimelapseRuntime.this.studio.getActivity().record(event);
            }
        });
    }

    public synchronized void start() {
        if (closed) throw new IllegalStateException("Build Timelapse runtime is shut down");
        if (started) return;
        MinecraftForge.EVENT_BUS.register(tickHandler);
        MinecraftForge.EVENT_BUS.register(this);
        blockEvents.register();
        started = true;
    }

    public void openStudio(Minecraft minecraft, StudioController controller, StudioScreenModel model) {
        if (minecraft == null || controller == null || model == null) {
            throw new IllegalArgumentException("minecraft/controller/model");
        }
        if (minecraft.theWorld == null || minecraft.thePlayer == null) {
            throw new IllegalStateException("Build Timelapse Studio can only open inside a loaded world");
        }
        start();
        try {
            minecraft.displayGuiScreen(new BuildTimelapseStudioScreen(controller, model, this));
        } catch (RuntimeException failure) {
            shutdown();
            throw failure;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && minecraftWorldUnavailable()) {
            shutdown();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote) shutdown();
    }

    private boolean minecraftWorldUnavailable() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc == null || mc.theWorld == null || mc.thePlayer == null;
    }

    public synchronized boolean isStarted() {
        return started && !closed;
    }

    public synchronized void shutdown() {
        if (closed) return;
        closed = true;
        if (started) {
            MinecraftForge.EVENT_BUS.unregister(tickHandler);
            MinecraftForge.EVENT_BUS.unregister(blockEvents);
            MinecraftForge.EVENT_BUS.unregister(this);
            started = false;
        }
        blockEvents.setListener(null);
        studio.getController().shutdown();
    }
}
