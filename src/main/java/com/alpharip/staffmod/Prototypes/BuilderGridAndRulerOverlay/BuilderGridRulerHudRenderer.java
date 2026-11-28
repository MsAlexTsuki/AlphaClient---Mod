package com.alpharip.staffmod.buildergridandruleroverlay;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Small ruler readout showing offsets from the saved world origin. */
public final class BuilderGridRulerHudRenderer {
    private BuilderGridRulerHudRenderer() { }

    public static void render(BuilderGridSnapshot snapshot, RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (snapshot == null || event == null || mc.thePlayer == null
                || mc.theWorld == null || mc.currentScreen != null
                || !snapshot.isRulerEnabled()) return;
        String line = "Builder Ruler  X: " + snapshot.getOffsetX()
                + "  Y: " + snapshot.getOffsetY()
                + "  Z: " + snapshot.getOffsetZ()
                + "  Grid: " + snapshot.getGridSpacing();
        mc.fontRendererObj.drawStringWithShadow(line, 6, 6, 0xFFFFD36A);
    }
}
