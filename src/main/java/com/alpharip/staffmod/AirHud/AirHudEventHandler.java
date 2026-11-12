package com.alpharip.staffmod.AirHud;

/** Dedicated Forge event listener boundary for the Air HUD renderer. */
final class AirHudEventHandler {
    private final AirHudRenderer renderer;

    AirHudEventHandler(AirHudRenderer renderer) {
        this.renderer = renderer;
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void onOverlay(net.minecraftforge.client.event.RenderGameOverlayEvent.Text event) {
        renderer.onOverlay(event);
    }
}
