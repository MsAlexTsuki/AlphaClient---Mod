package com.alpharip.staffmod.armorbreakwarning;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;

/**
 * Standalone Armor Break Warning prototype.
 * Registration is explicit and independent of AlphaClient Core.
 */
public final class ArmorBreakWarning {
    private static final ArmorBreakWarning INSTANCE = new ArmorBreakWarning();

    private final ArmorBreakWarningSettings settings = new ArmorBreakWarningSettings();
    private final ArmorBreakWarningRenderer renderer = new ArmorBreakWarningRenderer(settings);
    private final ArmorBreakWarningEventHandler eventHandler = new ArmorBreakWarningEventHandler(renderer);
    private final ArmorBreakWarningStorage storage = new ArmorBreakWarningStorage();

    private boolean registered;
    private boolean loaded;

    private ArmorBreakWarning() { }

    public static ArmorBreakWarning getInstance() {
        return INSTANCE;
    }

    public ArmorBreakWarningSettings getSettings() {
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
            mc.displayGuiScreen(new ArmorBreakWarningGui(parent, settings, this));
        }
    }

    synchronized void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.save(new File(mc.mcDataDir, "config/alphaclient/armor-break-warning.properties"), settings);
    }

    private synchronized void load() {
        if (loaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.load(new File(mc.mcDataDir, "config/alphaclient/armor-break-warning.properties"), settings);
        loaded = true;
    }

    public boolean isRegistered() {
        return registered;
    }
}
