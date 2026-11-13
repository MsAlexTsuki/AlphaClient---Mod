package com.alpharip.staffmod.TimeTill;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Forge overlay event adapter for the standalone TimeTill prototype. */
final class TimeTillEventHandler {
    private final TimeTillRenderer renderer;

    TimeTillEventHandler(TimeTillRenderer renderer) {
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        renderer.onOverlay(event);
    }
}
