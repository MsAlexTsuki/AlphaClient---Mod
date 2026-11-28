package com.alpharip.staffmod.playerhistory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standalone Player History prototype.
 *
 * The original lightweight record API is retained for source compatibility.
 * Live tracking observes only player entries exposed by this local client.
 */
public final class PlayerHistory {
    public static final String NAME = "Player History";
    public static final String CATEGORY = "StaffOps";

    private static final PlayerHistory INSTANCE = new PlayerHistory();

    private final PlayerHistoryStore store = new PlayerHistoryStore();
    private final PlayerHistoryTracker tracker = new PlayerHistoryTracker(this);
    private final PlayerHistoryEventHandler eventHandler =
            new PlayerHistoryEventHandler(this);
    private final PlayerHistoryStorage storage = new PlayerHistoryStorage();
    private final List<String> records = new ArrayList<String>();

    private boolean enabled = true;
    private boolean registered;
    private boolean storageLoaded;

    private PlayerHistory() { }

    public static PlayerHistory getInstance() {
        return INSTANCE;
    }

    public synchronized void register() {
        if (registered) return;
        loadStorageIfNeeded();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().register(eventHandler);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        tracker.stopAndClose();
        save();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().unregister(eventHandler);
        registered = false;
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (!enabled) {
            tracker.stopAndClose();
            save();
        } else {
            tracker.reset();
        }
    }

    /** Called by the Forge client-tick adapter. */
    void onClientTick() {
        if (enabled) tracker.update();
    }

    public PlayerHistoryStore getStore() {
        loadStorageIfNeeded();
        return store;
    }

    public PlayerHistoryGui createGui(GuiScreen parent) {
        return new PlayerHistoryGui(parent, this);
    }

    /** Opens the standalone screen; no menu button or keybind is installed here. */
    public void openGui(GuiScreen parent) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null) minecraft.displayGuiScreen(createGui(parent));
    }

    void save() {
        if (!storageLoaded) return;
        storage.save(getStorageFile(), store);
    }

    private synchronized void loadStorageIfNeeded() {
        if (storageLoaded) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.mcDataDir == null) return;
        storage.load(new File(minecraft.mcDataDir, "config/alphaclient/player-history.properties"), store);
        storageLoaded = true;
    }

    private File getStorageFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.mcDataDir == null) return null;
        return new File(minecraft.mcDataDir, "config/alphaclient/player-history.properties");
    }

    // Retained original API: these are caller-supplied notes, not live player events.
    public synchronized void addRecord(String value) {
        if (value != null && !value.trim().isEmpty()) records.add(value);
    }

    public synchronized List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public synchronized void clearRecords() {
        records.clear();
    }

    /** Clears tracked player history and persists the empty local store. */
    public void clearHistory() {
        store.clear();
        save();
    }

    public boolean isRegistered() {
        return registered;
    }
}
