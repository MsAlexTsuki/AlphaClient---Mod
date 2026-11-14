package com.alpharip.staffmod.rememberchestinventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;
import java.io.IOException;

/**
 * Standalone Remember Chest Inventory prototype.
 *
 * The snapshot model, search, bounded history, and local persistence are implemented.
 * A Minecraft 1.8.9 chest-open/inventory adapter and module registration are not wired yet.
 */
public final class RememberChestInventory {
    private static final RememberChestInventory INSTANCE = new RememberChestInventory();

    private final ChestMemoryManager manager = new ChestMemoryManager();
    private final ChestMemoryScanner scanner = new ChestMemoryScanner();
    private final ChestMemoryStorage storage = new ChestMemoryStorage();
    private boolean loaded;

    private RememberChestInventory() { }

    public static RememberChestInventory getInstance() { return INSTANCE; }
    public ChestMemoryManager getManager() { load(); return manager; }
    public ChestMemoryScanner getScanner() { return scanner; }

    public void remember(ChestMemoryEntry entry) {
        load();
        manager.remember(entry);
        save();
    }

    public void openGui(GuiScreen parent) {
        load();
        Minecraft.getMinecraft().displayGuiScreen(new RememberChestInventoryGui(parent, this));
    }

    public synchronized void load() {
        if (loaded) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.mcDataDir == null) return;
        File file = getStorageFile(minecraft.mcDataDir);
        try { storage.load(file, manager); }
        catch (IOException ignored) {
            // Keep safe defaults if the local history is missing or unreadable.
        }
        loaded = true;
    }

    public synchronized void save() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.mcDataDir == null) return;
        try { storage.save(getStorageFile(minecraft.mcDataDir), manager); }
        catch (IOException ignored) {
            // Keep the in-memory history available for this session.
        }
    }

    private File getStorageFile(File gameDirectory) {
        return new File(gameDirectory, "config/alphaclient/remember-chest-inventory.properties");
    }
}