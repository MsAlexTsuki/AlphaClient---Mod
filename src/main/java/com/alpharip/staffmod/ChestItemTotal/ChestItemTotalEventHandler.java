package com.alpharip.staffmod.ChestItemTotal;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Updates chest totals and renders the summary while a chest screen is open. */
final class ChestItemTotalEventHandler {
    private final ChestItemTotalScanner scanner;
    private final ChestItemTotalRenderer renderer;

    ChestItemTotalEventHandler(ChestItemTotalScanner scanner,
                               ChestItemTotalRenderer renderer) {
        this.scanner = scanner;
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        scanner.refresh();
        renderer.render(event.gui);
    }
}
