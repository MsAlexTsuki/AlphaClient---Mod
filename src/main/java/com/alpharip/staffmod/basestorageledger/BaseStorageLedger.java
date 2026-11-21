package com.alpharip.staffmod.basestorageledger;

import com.alpharip.staffmod.rememberchestinventory.ChestMemoryEntry;
import com.alpharip.staffmod.rememberchestinventory.RememberChestInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Base Storage Ledger prototype.
 *
 * It aggregates existing RememberChestInventory snapshots. It does not open or scan
 * containers itself, and it does not modify the existing chest-memory module.
 */
public final class BaseStorageLedger {
    private static final BaseStorageLedger INSTANCE = new BaseStorageLedger();
    private static final long STALE_AFTER_MILLIS = 24L * 60L * 60L * 1000L;

    private final StorageLedgerIndex index = new StorageLedgerIndex();
    private final StorageLowAlert lowAlert = new StorageLowAlert();
    private boolean settingsLoaded;

    private BaseStorageLedger() { }

    public static BaseStorageLedger getInstance() { return INSTANCE; }
    public StorageLedgerIndex getIndex() { return index; }
    public StorageLowAlert getLowAlert() { loadSettings(); return lowAlert; }

    public synchronized void refresh() {
        List<ChestMemoryEntry> snapshots =
                RememberChestInventory.getInstance().getManager().getEntries();
        index.rebuild(snapshots);
    }

    public void openGui(GuiScreen parent) {
        loadSettings();
        refresh();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) mc.displayGuiScreen(new BaseStorageLedgerGui(parent, this));
    }

    public synchronized void saveSettings() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        try {
            lowAlert.save(getSettingsFile(mc.mcDataDir));
        } catch (IOException ex) {
            // Keep thresholds in memory; the GUI remains usable for this session.
        }
    }

    public synchronized void loadSettings() {
        if (settingsLoaded) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) return;
        try {
            lowAlert.load(getSettingsFile(mc.mcDataDir));
        } catch (IOException ex) {
            // Keep safe defaults if the local settings file is missing or unreadable.
        }
        settingsLoaded = true;
    }

    public long getStaleAfterMillis() { return STALE_AFTER_MILLIS; }

    private File getSettingsFile(File gameDirectory) {
        return new File(gameDirectory, "config/alphaclient/base-storage-ledger.properties");
    }
}
