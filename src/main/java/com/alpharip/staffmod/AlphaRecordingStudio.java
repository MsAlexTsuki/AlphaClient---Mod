package com.alpharip.staffmod;

import com.alpharip.staffmod.RecordingStudio.command.RecordingStudioCommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * Standalone Forge entry point for Alpha Recording Studio.
 * Recording Studio remains separate from the AlphaClient mod lifecycle.
 */
@Mod(
        modid = AlphaRecordingStudio.MOD_ID,
        name = AlphaRecordingStudio.NAME,
        version = AlphaRecordingStudio.VERSION,
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
