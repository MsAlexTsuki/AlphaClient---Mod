package com.alpharip.staffmod;

import com.alpharip.staffmod.RecordingStudio.command.RecordingStudioCommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

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

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new RecordingStudioCommand());
    }
}
