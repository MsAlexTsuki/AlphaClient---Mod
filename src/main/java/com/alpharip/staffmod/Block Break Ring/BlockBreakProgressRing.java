package com.alpharip.staffmod.blockbreakring;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

/**
 * Standalone Block Break Progress Ring module.
 *
 * Call start() when this prototype is enabled and stop() when it is disabled.
 * No Alpha Client Core registration is performed here.
 */
public final class BlockBreakProgressRing {
    public static final String NAME = "Block Break Progress Ring";
    public static final String CATEGORY = "Visuals";
    public static final boolean REQUIRES_SUPABASE = false;

    private final BlockBreakRingConfig config;
    private final BlockBreakRingAnimation animation;
    private final BlockBreakRingRenderer renderer;
    private final BlockBreakRingEventHandler eventHandler;
    private boolean running;

    public BlockBreakProgressRing() {
        this(new BlockBreakRingConfig());
    }

    public BlockBreakProgressRing(BlockBreakRingConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        this.config = config;
        this.animation = new BlockBreakRingAnimation();
        this.renderer = new BlockBreakRingRenderer(config);
        this.eventHandler = new BlockBreakRingEventHandler(this, animation, renderer);
    }

    /** Registers only this module's own Forge event handler. */
    public void start() {
        if (running) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        running = true;
    }

    /** Unregisters the module and clears transient render state. */
    public void stop() {
        if (!running) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        animation.reset();
        running = false;
    }

    public boolean isRunning() { return running; }
    public boolean isEnabled() { return config.isEnabled(); }

    public void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
        if (!enabled) animation.reset();
    }

    /** Current smoothed progress in the range 0..1. */
    public float getProgress() { return animation.getProgress(); }

    /** Compatibility/testing input; normal runtime progress comes from Forge events. */
    public void setProgress(float progress) { animation.setProgress(progress); }

    public int getLineWidth() { return config.getLineWidth(); }
    public void setLineWidth(int width) { config.setLineWidth(width); }
    public BlockBreakRingConfig getConfig() { return config; }

    public BlockBreakRingSnapshot getSnapshot() {
        return new BlockBreakRingSnapshot(config.isEnabled(), running, animation.getProgress(),
                config.getLineWidth(), config.getRadius(), config.getColorIndex(),
                config.getOpacity(), config.isShowBackground(), config.isSmoothAnimation());
    }

    /** Opens the module's standalone settings screen. */
    public void openSettings(GuiScreen parent) {
        Minecraft.getMinecraft().displayGuiScreen(new BlockBreakRingGui(parent, config));
    }

    // TODO(Supabase): If this module later needs account-synced preferences,
    // document the required table/columns and RLS policies here. Do not add
    // Supabase calls or alter database state in this prototype.
}
