package com.alpharip.staffmod.TimeTill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;

/**
 * Standalone TimeTill prototype.
 * Register this module explicitly from a future integration point.
 */
public final class timetillsunsetornight {
    private static final timetillsunsetornight INSTANCE = new timetillsunsetornight();

    private final TimeTillSettings settings = new TimeTillSettings();
    private final TimeTillRenderer renderer = new TimeTillRenderer(settings);
    private final TimeTillEventHandler eventHandler = new TimeTillEventHandler(renderer);
    private final TimeTillStorage storage = new TimeTillStorage();

    private boolean registered;
    private boolean loaded;

    private timetillsunsetornight() { }

    public static timetillsunsetornight getInstance() {
        return INSTANCE;
    }

    public TimeTillSettings getSettings() {
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
            mc.displayGuiScreen(new TimeTillGui(parent, settings, this));
        }
    }

    synchronized void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.save(new File(mc.mcDataDir, "config/alphaclient/timetill.properties"), settings);
    }

    private synchronized void load() {
        if (loaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.load(new File(mc.mcDataDir, "config/alphaclient/timetill.properties"), settings);
        loaded = true;
    }

    public boolean isRegistered() {
        return registered;
    }
}
