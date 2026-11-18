package com.alpharip.staffmod.BuildTimelapse.integration;

import com.alpharip.staffmod.BuildTimelapse.BuildTimelapse;
import com.alpharip.staffmod.BuildTimelapse.ui.BuildTimelapseStudioScreen;
import com.alpharip.staffmod.BuildTimelapse.ui.StudioController;
import com.alpharip.staffmod.BuildTimelapse.ui.StudioScreenModel;
import com.alpharip.staffmod.RecordingStudio.camera.FileCameraPathStore;
import com.alpharip.staffmod.RecordingStudio.module.RecordingStudioModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;

/**
 * Owns Forge event registration for one Build Timelapse session.
 * The runtime remains available for the current world after its Studio screen closes,
 * allowing Recording Studio to use the registered camera-path adapter.
 */
public final class BuildTimelapseRuntime {
    private final BuildTimelapse studio;
    private final BuildTimelapseClientTickHandler tickHandler;
    private final ForgeMinecraftBlockEventBridge blockEvents = new ForgeMinecraftBlockEventBridge();
    private BuildTimelapseRecordingModuleAdapter recordingModule;
    private boolean started;
    private boolean blockEventsRegistered;
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

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.mcDataDir == null) {
            throw new IllegalStateException("Minecraft client storage is unavailable");
        }
        recordingModule = new BuildTimelapseRecordingModuleAdapter(
                studio.getController(),
                new FileCameraPathStore(new File(minecraft.mcDataDir, "buildtimelapse/camera-paths")));
        RecordingStudioModuleRegistry.register(recordingModule);

        MinecraftForge.EVENT_BUS.register(tickHandler);
        MinecraftForge.EVENT_BUS.register(this);
        started = true;
    }

    private synchronized void registerBlockEvents() {
        if (!started || blockEventsRegistered) return;
        MinecraftForge.EVENT_BUS.register(blockEvents);
        blockEventsRegistered = true;
    }

    private synchronized void unregisterBlockEvents() {
        if (!blockEventsRegistered) return;
        MinecraftForge.EVENT_BUS.unregister(blockEvents);
        blockEventsRegistered = false;
    }

    public void openStudio(Minecraft minecraft, StudioController controller, StudioScreenModel model) {
        if (minecraft == null || controller == null || model == null) {
            throw new IllegalArgumentException("minecraft/controller/model");
        }
        if (minecraft.theWorld == null || minecraft.thePlayer == null) {
            throw new IllegalStateException("Build Timelapse Studio can only open inside a loaded world");
        }
        start();
        registerBlockEvents();
        try {
            minecraft.displayGuiScreen(new BuildTimelapseStudioScreen(controller, model, this));
        } catch (RuntimeException failure) {
            shutdown();
            throw failure;
        }
    }

    /** Releases the Build Timelapse camera and block tracker but keeps the module adapter registered. */
    public synchronized void closeStudio() {
        if (closed || !started) return;
        studio.getController().stop();
        unregisterBlockEvents();
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
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft == null || minecraft.theWorld == null || minecraft.thePlayer == null;
    }

    public synchronized boolean isStarted() {
        return started && !closed;
    }

    public synchronized void shutdown() {
        if (closed) return;
        closed = true;
        if (recordingModule != null) {
            RecordingStudioModuleRegistry.unregister(recordingModule);
            recordingModule = null;
        }
        unregisterBlockEvents();
        if (started) {
            MinecraftForge.EVENT_BUS.unregister(tickHandler);
            MinecraftForge.EVENT_BUS.unregister(this);
            started = false;
        }
        blockEvents.setListener(null);
        studio.getController().shutdown();
    }
}
