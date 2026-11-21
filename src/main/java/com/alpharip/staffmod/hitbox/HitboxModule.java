package com.alpharip.staffmod.hitbox;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Isolated Hitbox prototype entry point.
 *
 * Register this instance on the Forge event bus when integrating the prototype.
 * It is intentionally not registered from AlphaClient yet.
 */
public final class HitboxModule {
    private final HitboxSettings settings;
    private final HitboxRenderer renderer;
    private boolean registered;

    public HitboxModule() {
        this(new HitboxSettings(), new HitboxRenderer());
    }

    public HitboxModule(HitboxSettings settings, HitboxRenderer renderer) {
        if (settings == null || renderer == null) {
            throw new IllegalArgumentException("Settings and renderer are required.");
        }
        this.settings = settings;
        this.renderer = renderer;
    }

    public HitboxSettings getSettings() {
        return settings;
    }

    public boolean isEnabled() {
        return settings.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        settings.setEnabled(enabled);
    }

    public void register() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = true;
        }
    }

    public void unregister() {
        if (registered) {
            MinecraftForge.EVENT_BUS.unregister(this);
            registered = false;
        }
    }

    public boolean isRegistered() {
        return registered;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        renderer.render(event, settings);
    }
}
