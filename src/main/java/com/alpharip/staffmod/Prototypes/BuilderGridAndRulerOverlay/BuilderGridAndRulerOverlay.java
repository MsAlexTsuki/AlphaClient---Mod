package com.alpharip.staffmod.buildergridandruleroverlay;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

/** Standalone builder grid and ruler prototype; not registered by AlphaClient Core. */
public final class BuilderGridAndRulerOverlay {
    public static final String NAME = "Builder Grid & Ruler Overlay";
    public static final String CATEGORY = "World/Building";
    private final BuilderGridSettings settings = new BuilderGridSettings();
    private final BuilderGridAnchor anchor = new BuilderGridAnchor();
    private final BuilderGridStorage storage = new BuilderGridStorage();
    private final BuilderGridEventHandler eventHandler = new BuilderGridEventHandler(this);
    private final File settingsFile;
    private boolean running;

    public BuilderGridAndRulerOverlay() {
        File root = Minecraft.getMinecraft().mcDataDir == null
                ? new File(".") : Minecraft.getMinecraft().mcDataDir;
        settingsFile = new File(root, "config/alphaclient/builder_grid_ruler.properties");
        storage.load(settingsFile, settings, anchor);
    }

    /** Registers Forge render listeners explicitly. Does not enable either overlay. */
    public void start() {
        if (running) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        running = true;
    }

    public void stop() {
        if (!running) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        running = false;
        saveSettings();
    }

    public boolean isRunning() { return running; }
    public BuilderGridSettings getSettings() { return settings; }
    public BuilderGridAnchor getAnchor() { return anchor; }

    public BuilderGridSnapshot createSnapshot(int playerX, int playerY, int playerZ) {
        if (!running || (!settings.isGridEnabled() && !settings.isRulerEnabled())) return null;
        return new BuilderGridSnapshot(settings, anchor, playerX, playerY, playerZ);
    }

    public int snapCoordinate(int coordinate) {
        return BuilderGridCalculator.snap(coordinate, settings.getGridSpacing());
    }

    public double snap(double coordinate) {
        return BuilderGridCalculator.snap(coordinate, settings.getGridSpacing());
    }

    public void captureAnchorFromPlayer() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        anchor.set((int) Math.floor(mc.thePlayer.posX),
                (int) Math.floor(mc.thePlayer.posY),
                (int) Math.floor(mc.thePlayer.posZ));
    }

    public void openSettings(GuiScreen parent) {
        Minecraft.getMinecraft().displayGuiScreen(new BuilderGridGui(this, parent));
    }

    public void saveSettings() {
        storage.save(settingsFile, settings, anchor);
    }
}
