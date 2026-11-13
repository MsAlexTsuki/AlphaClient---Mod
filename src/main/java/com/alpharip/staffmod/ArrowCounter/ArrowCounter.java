package com.alpharip.staffmod.ArrowCounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;

/**
 * Standalone ArrowCounter prototype.
 * Register explicitly from a future integration point; no AlphaClient Core dependency.
 */
public final class ArrowCounter {
    private static final ArrowCounter INSTANCE = new ArrowCounter();

    private final ArrowCounterSettings settings = new ArrowCounterSettings();
    private final ArrowCounterScanner scanner = new ArrowCounterScanner();
    private final ArrowCounterRenderer renderer = new ArrowCounterRenderer(settings, scanner);
    private final ArrowCounterEventHandler eventHandler =
            new ArrowCounterEventHandler(scanner, renderer);
    private final ArrowCounterStorage storage = new ArrowCounterStorage();

    private boolean registered;
    private boolean loaded;

    private ArrowCounter() { }

    public static ArrowCounter getInstance() {
        return INSTANCE;
    }

    public ArrowCounterSettings getSettings() {
        return settings;
    }

    public int getArrowCount() {
        return scanner.getArrowCount();
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
            mc.displayGuiScreen(new ArrowCounterGui(parent, settings, this));
        }
    }

    synchronized void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.save(new File(mc.mcDataDir, "config/alphaclient/arrow-counter.properties"), settings);
    }

    private synchronized void load() {
        if (loaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.load(new File(mc.mcDataDir, "config/alphaclient/arrow-counter.properties"), settings);
        loaded = true;
    }

    public boolean isRegistered() {
        return registered;
    }
}
