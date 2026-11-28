package com.alpharip.staffmod.remembervillagers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

/**
 * Standalone Remember Villagers prototype.
 * Keeps a local record of villager trades without depending on AlphaClient Core or Supabase.
 */
public final class remembervillagertrade {
    private static final remembervillagertrade INSTANCE = new remembervillagertrade();

    private final VillagerTradeRegistry registry = new VillagerTradeRegistry();
    private final VillagerTradeStorage storage = new VillagerTradeStorage();
    private final VillagerTradeCapture capture = new VillagerTradeCapture(registry);
    private final VillagerTradeEventHandler eventHandler = new VillagerTradeEventHandler(capture);
    private boolean registered;
    private boolean loaded;

    private remembervillagertrade() { }

    public static remembervillagertrade getInstance() {
        return INSTANCE;
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

    public VillagerTradeRegistry getRegistry() {
        load();
        return registry;
    }

    public void openGui(GuiScreen parent) {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.displayGuiScreen(new RememberVillagerTradesGui(parent, registry, this));
        }
    }

    synchronized void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.save(new File(mc.mcDataDir, "config/alphaclient/remember-villagers.properties"), registry);
    }

    private synchronized void load() {
        if (loaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        storage.load(new File(mc.mcDataDir, "config/alphaclient/remember-villagers.properties"), registry);
        loaded = true;
    }

    public boolean isRegistered() {
        return registered;
    }
}
