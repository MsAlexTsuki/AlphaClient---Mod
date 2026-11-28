package com.alpharip.staffmod.AirHud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.block.material.Material;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Renders remaining air while the local player is underwater. */
public final class AirHudRenderer {
    private final AirHudSettings settings;

    AirHudRenderer(AirHudSettings settings) {
        this.settings = settings;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!settings.isEnabled() || mc.thePlayer == null || mc.theWorld == null
                || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        if (!mc.thePlayer.isInsideOfMaterial(Material.water)) return;

        int airTicks = Math.max(0, mc.thePlayer.getAir());
        int seconds = (airTicks + 19) / 20;
        String value = airTicks + " ticks (" + seconds + "s)";
        String text = settings.isShowLabel() ? "Air: " + value : value;
        ScaledResolution resolution = new ScaledResolution(mc);
        int textWidth = mc.fontRendererObj.getStringWidth(text);
        int x;
        int y;
        switch (settings.getPosition()) {
            case TOP_RIGHT:
                x = resolution.getScaledWidth() - textWidth - 6;
                y = 6;
                break;
            case BOTTOM_RIGHT:
                x = resolution.getScaledWidth() - textWidth - 6;
                y = resolution.getScaledHeight() - mc.fontRendererObj.FONT_HEIGHT - 6;
                break;
            case TOP_LEFT:
                x = 6;
                y = 6;
                break;
            default:
                x = 6;
                y = resolution.getScaledHeight() - mc.fontRendererObj.FONT_HEIGHT - 6;
                break;
        }
        mc.fontRendererObj.drawStringWithShadow(text, x, y, settings.getColor());
    }
}
