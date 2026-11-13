package com.alpharip.staffmod.ArrowCounter;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Refreshes inventory totals and forwards the HUD overlay event. */
final class ArrowCounterEventHandler {
    private final ArrowCounterScanner scanner;
    private final ArrowCounterRenderer renderer;

    ArrowCounterEventHandler(ArrowCounterScanner scanner, ArrowCounterRenderer renderer) {
        this.scanner = scanner;
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            scanner.refresh();
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        renderer.onOverlay(event);
    }
}
