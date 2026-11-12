package com.alpharip.staffmod.BiomeHud;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Forge overlay event adapter for the standalone Biome HUD prototype. */
final class BiomeHudEventHandler {
    private final BiomeHudRenderer renderer;

    BiomeHudEventHandler(BiomeHudRenderer renderer) {
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        renderer.onOverlay(event);
    }
}
