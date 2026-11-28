package com.alpharip.staffmod.ChestItemTotal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

/**
 * Standalone ChestItemTotal prototype.
 * Register explicitly from a future integration point; no AlphaClient Core dependency.
 */
public final class ChestItemTotal {
    private static final ChestItemTotal INSTANCE = new ChestItemTotal();

    private final ChestItemTotalSettings settings = new ChestItemTotalSettings();
    private final ChestItemTotalScanner scanner = new ChestItemTotalScanner();
    private final ChestItemTotalRenderer renderer = new ChestItemTotalRenderer(settings, scanner);
    private final ChestItemTotalEventHandler eventHandler =
            new ChestItemTotalEventHandler(scanner, renderer);
    private final ChestItemTotalStorage storage = new ChestItemTotalStorage();
    private boolean registered;
    private boolean loaded;

    private ChestItemTotal() { }

    public static ChestItemTotal getInstance() {
        return INSTANCE;
    }

    public ChestItemTotalSettings getSettings() {
        return settings;
    }

    public ChestItemTotalSnapshot getSnapshot() {
        return scanner.getSnapshot();
    }

    public synchronized void register() {
        if (registered) return;
        load();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        save();
        registered = false;
    }

    public void openGui(GuiScreen parent) {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.displayGuiScreen(new ChestItemTotalGui(parent, settings, this));
        }
    }

    synchronized void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.save(new File(mc.mcDataDir,
                "config/alphaclient/chest-item-total.properties"), settings);
    }

    private synchronized void load() {
        if (loaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.load(new File(mc.mcDataDir,
                "config/alphaclient/chest-item-total.properties"), settings);
        loaded = true;
    }

    public boolean isRegistered() {
        return registered;
    }
}
