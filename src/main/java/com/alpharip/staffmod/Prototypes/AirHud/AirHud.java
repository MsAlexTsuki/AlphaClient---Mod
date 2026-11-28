package com.alpharip.staffmod.AirHud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;

/** Standalone Air HUD prototype; registration is explicit and independent of AlphaClient Core. */
public final class AirHud {
    private static final AirHud INSTANCE = new AirHud();
    private final AirHudSettings settings = new AirHudSettings();
    private final AirHudRenderer renderer = new AirHudRenderer(settings);
    private final AirHudEventHandler eventHandler = new AirHudEventHandler(renderer);
    private final AirHudStorage storage = new AirHudStorage();
    private boolean registered;
    private boolean loaded;

    private AirHud() { }

    public static AirHud getInstance() { return INSTANCE; }
    public AirHudSettings getSettings() { return settings; }

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
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) mc.displayGuiScreen(new AirHudGui(parent, settings, this));
    }

    synchronized void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.save(new File(mc.mcDataDir, "config/alphaclient/air-hud.properties"), settings);
    }

    private synchronized void load() {
        if (loaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.load(new File(mc.mcDataDir, "config/alphaclient/air-hud.properties"), settings);
        loaded = true;
    }

    public boolean isRegistered() { return registered; }
}
