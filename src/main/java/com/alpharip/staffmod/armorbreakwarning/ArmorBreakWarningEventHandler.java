package com.alpharip.staffmod.armorbreakwarning;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Forge overlay adapter for Armor Break Warning. */
final class ArmorBreakWarningEventHandler {
    private final ArmorBreakWarningRenderer renderer;

    ArmorBreakWarningEventHandler(ArmorBreakWarningRenderer renderer) {
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        renderer.onOverlay(event);
    }
}
