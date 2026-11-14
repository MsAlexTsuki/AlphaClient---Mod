package com.alpharip.staffmod.autosave;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

/**
 * Standalone AutoSave prototype entry point.
 * Observes Forge world-save events and exposes a configurable HUD and GUI.
 */
public final class AutoSave {
    private static final AutoSave INSTANCE = new AutoSave();

    private final AutoSaveSettings settings = new AutoSaveSettings();
    private final AutoSaveState state = new AutoSaveState();
    private final AutoSaveStorage storage = new AutoSaveStorage();
    private final AutoSaveHud hud = new AutoSaveHud(settings, state);
    private final AutoSaveEventHandler eventHandler = new AutoSaveEventHandler(this);

    private AutoSave() {
        storage.load(getSettingsFile(), settings);
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    public static AutoSave getInstance() {
        return INSTANCE;
    }

    public AutoSaveSettings getSettings() { return settings; }
    public AutoSaveState getState() { return state; }

    AutoSaveHud getHud() { return hud; }

    void onWorldSaved(String worldName) {
        state.recordSave(worldName, System.currentTimeMillis());
    }

    public void openGui(GuiScreen parent) {
        Minecraft.getMinecraft().displayGuiScreen(new AutoSaveGui(parent, this));
    }

    public void save() {
        storage.save(getSettingsFile(), settings);
    }

    private File getSettingsFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File root = mc.mcDataDir == null ? new File(".") : mc.mcDataDir;
        return new File(root, "config/alphaclient/autosave.properties");
    }
}
