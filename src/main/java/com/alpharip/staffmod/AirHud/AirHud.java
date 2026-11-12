package com.alpharip.staffmod.AirHud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;

/** Standalone Air HUD prototype; registration is explicit and does not touch AlphaClient Core. */
public final class AirHud {
    private static final AirHud INSTANCE = new AirHud();
    private final AirHudSettings settings = new AirHudSettings();
    private final AirHudRenderer renderer = new AirHudRenderer(settings);
    private final AirHudEventHandler eventHandler = new AirHudEventHandler(renderer);
    private boolean registered;

    private AirHud() { }

    public static AirHud getInstance() { return INSTANCE; }
    public AirHudSettings getSettings() { return settings; }

    public synchronized void register() {
        if (registered) return;
        load();
        MinecraftForgeBridge.register(eventHandler);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        MinecraftForgeBridge.unregister(eventHandler);
        save();
        registered = false;
    }

    public void openGui(GuiScreen parent) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) mc.displayGuiScreen(new AirHudGui(parent, settings, this));
    }

    void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        new AirHudStorage().save(new File(mc.mcDataDir, "config/alphaclient/air-hud.properties"), settings);
    }

    private void load() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        new AirHudStorage().load(new File(mc.mcDataDir, "config/alphaclient/air-hud.properties"), settings);
    }

    public boolean isRegistered() { return registered; }
}
