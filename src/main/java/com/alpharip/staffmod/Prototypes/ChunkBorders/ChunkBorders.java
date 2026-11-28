package com.alpharip.staffmod.chunkborders;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

/** Standalone Chunk Borders prototype; not registered by AlphaClient Core. */
public final class ChunkBorders {
    public static final String NAME = "Chunk Borders";
    public static final String CATEGORY = "World/diagnostics";
    public static final boolean REQUIRES_SUPABASE = false;

    private final ChunkBordersSettings settings;
    private final ChunkBordersStorage storage;
    private final File settingsFile;
    private final ChunkBordersEventHandler eventHandler;
    private boolean running;

    public ChunkBorders() {
        settings = new ChunkBordersSettings();
        storage = new ChunkBordersStorage();
        eventHandler = new ChunkBordersEventHandler(this);
        File root = Minecraft.getMinecraft().mcDataDir == null
                ? new File(".") : Minecraft.getMinecraft().mcDataDir;
        settingsFile = new File(root, "config/alphaclient/chunk_borders.properties");
        storage.load(settingsFile, settings);
    }

    /** Registers the world-render adapter explicitly. Does not enable the module. */
    public void start() {
        if (running) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        running = true;
    }

    public void stop() {
        if (!running) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        running = false;
        saveSettings();
    }

    public boolean isRunning() { return running; }
    public boolean isEnabled() { return settings.isEnabled(); }
    public ChunkBordersSettings getSettings() { return settings; }

    public ChunkBordersSnapshot createSnapshot(int playerChunkX, int playerChunkZ) {
        if (!running || !settings.isEnabled()) return null;
        return new ChunkBordersSnapshot(settings, playerChunkX, playerChunkZ);
    }

    public void openSettings(GuiScreen parent) {
        Minecraft.getMinecraft().displayGuiScreen(new ChunkBordersGui(this, parent));
    }

    public void saveSettings() {
        storage.save(settingsFile, settings);
    }
}
