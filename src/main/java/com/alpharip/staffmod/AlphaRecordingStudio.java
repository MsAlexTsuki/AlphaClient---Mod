package com.alpharip.staffmod;

import com.alpharip.staffmod.BuildTimelapse.command.BuildTimelapseCommand;
import com.alpharip.staffmod.BuildTimelapse.integration.BuildTimelapseAddon;
import com.alpharip.staffmod.RecordingStudio.command.RecordingStudioCommand;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge entry point for the optional Alpha Recording Studio addon.
 * Alpha Client is the required core mod; Alpha Client itself does not depend on this addon.
 */
@Mod(
        modid = AlphaRecordingStudio.MOD_ID,
        name = AlphaRecordingStudio.NAME,
        version = AlphaRecordingStudio.VERSION,
        dependencies = "required-after:alphaclient",
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]"
)
public final class AlphaRecordingStudio {
    public static final String MOD_ID = "alpharecordingstudio";
    public static final String NAME = "Alpha Recording Studio";
    public static final String VERSION = "0.1.0";
    private static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new RecordingStudioCommand());
        ClientCommandHandler.instance.registerCommand(new BuildTimelapseCommand());
        MinecraftForge.EVENT_BUS.register(this);
    }

    /** Build Timelapse's module adapter is available before the player opens Recording Studio. */
    @SubscribeEvent
    public void onClientWorldLoad(WorldEvent.Load event) {
        if (event.world == null || !event.world.isRemote) return;
        try {
            BuildTimelapseAddon.initialize(Minecraft.getMinecraft());
        } catch (Exception failure) {
            LOGGER.error("Could not initialize the Build Timelapse addon runtime", failure);
        }
    }
}
