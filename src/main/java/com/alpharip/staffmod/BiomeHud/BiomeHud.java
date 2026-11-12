package com.alpharip.staffmod.BiomeHud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;

/**
 * Standalone Biome HUD prototype.
 * Registration is explicit and does not depend on AlphaClient Core.
 */
public final class BiomeHud {
    private static final BiomeHud INSTANCE = new BiomeHud();

    private final BiomeHudSettings settings = new BiomeHudSettings();
    private final BiomeHudRenderer renderer = new BiomeHudRenderer(settings);
    private final BiomeHudEventHandler eventHandler = new BiomeHudEventHandler(renderer);
    private final BiomeHudStorage storage = new BiomeHudStorage();

    private boolean registered;
    private boolean loaded;

    private BiomeHud() { }

    public static BiomeHud getInstance() {
        return INSTANCE;
    }

    public BiomeHudSettings getSettings() {
        return settings;
    }

    public synchronized void register() {
        if (registered) return;
        load();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(eventHandler);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(eventHandler);
        save();
        registered = false;
    }

    public void openGui(GuiScreen parent) {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.displayGuiScreen(new BiomeHudGui(parent, settings, this));
        }
    }

    synchronized void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.save(new File(mc.mcDataDir, "config/alphaclient/biome-hud.properties"), settings);
    }

    private synchronized void load() {
        if (loaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.load(new File(mc.mcDataDir, "config/alphaclient/biome-hud.properties"), settings);
        loaded = true;
    }

    public boolean isRegistered() {
        return registered;
    }
}
