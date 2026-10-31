// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Compatibility event bridge. Rendering is delegated to AlphaHudManager so
 * individual HUD elements can be registered and maintained independently.
 */
public final class AlphaHudRenderer {
    private final AlphaHudManager manager = new AlphaHudManager();

    public AlphaHudManager getManager() {
        return manager;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        manager.onOverlay(event);
    }
}
