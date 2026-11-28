package com.alpharip.staffmod.buildsymmetryguides;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;

/** Standalone prototype. AlphaClient Core does not register it automatically. */
public final class BuildSymmetryGuides {
    public static final String NAME = "Build Symmetry Guides";
    public static final String CATEGORY = "World";
    public static final boolean REQUIRES_SUPABASE = false;

    private final BuildSymmetryGuideConfig config;
    private final BuildSymmetryGuidesEventHandler eventHandler;
    private final File settingsFile;
    private boolean running;
    private SymmetryGuideSnapshot snapshot;

    public BuildSymmetryGuides() { this(new BuildSymmetryGuideConfig()); }

    public BuildSymmetryGuides(BuildSymmetryGuideConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        this.config = config;
        File directory = new File(Minecraft.getMinecraft().mcDataDir, "config/alphaclient");
        settingsFile = new File(directory, "build_symmetry_guides.properties");
        BuildSymmetryGuideStorage.load(settingsFile, config);
        eventHandler = new BuildSymmetryGuidesEventHandler(this);
    }

    public void start() {
        if (running) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        running = true;
    }

    public void stop() {
        if (!running) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        snapshot = null;
        running = false;
        saveSettings();
    }

    public boolean isRunning() { return running; }
    public boolean isEnabled() { return config.isEnabled(); }
    public BuildSymmetryGuideConfig getConfig() { return config; }
    public SymmetryGuideSnapshot getSnapshot() { return snapshot; }

    public void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
        if (!enabled) snapshot = null;
    }

    void updateTarget(BlockPos source) {
        if (source == null || !config.isEnabled()) {
            snapshot = null;
            return;
        }
        BlockCoordinates origin = new BlockCoordinates(config.getOriginX(), 0, config.getOriginZ());
        BlockCoordinates target = new BlockCoordinates(source.getX(), source.getY(), source.getZ());
        snapshot = new SymmetryGuideSnapshot(target, origin,
                SymmetryCalculator.calculate(target, origin, config.getAxis()), config);
    }

    void clearTarget() { snapshot = null; }

    public void adjustOrigin(int deltaX, int deltaZ) {
        config.setOrigin(saturatedAdd(config.getOriginX(), deltaX),
                saturatedAdd(config.getOriginZ(), deltaZ));
        snapshot = null;
    }

    private static int saturatedAdd(int value, int delta) {
        long result = (long) value + delta;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE
                : result < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) result;
    }

    public boolean setOriginToPlayer() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return false;
        config.setOrigin(MathHelper.floor_double(mc.thePlayer.posX),
                MathHelper.floor_double(mc.thePlayer.posZ));
        snapshot = null;
        return true;
    }

    public void saveSettings() { BuildSymmetryGuideStorage.save(settingsFile, config); }

    public void openSettings(GuiScreen parent) {
        Minecraft.getMinecraft().displayGuiScreen(new BuildSymmetryGuidesGui(this, parent));
    }
}
