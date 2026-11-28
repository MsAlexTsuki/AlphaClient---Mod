// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Compatibility bridge for the existing HUD renderer. */
public final class AlphaHudManager {
    public AlphaHudManager() { }

    public void onOverlay(RenderGameOverlayEvent.Text event) {
        // Retired legacy HUD widgets are intentionally not rendered here.
    }
}
